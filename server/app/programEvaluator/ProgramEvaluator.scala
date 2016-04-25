package programEvaluator

import java.lang.reflect.Type
import javassist.bytecode.stackmap.TypeTag

import leon.DefaultReporter
import leon.collection.Cons
import leon.evaluators.{AbstractEvaluator, EvaluationResults}
import leon.purescala.Definitions.{CaseClassDef, Program}
import leon.purescala.Expressions._
import leon.purescala.Types.CaseClassType
import leon.webDSL.webDescription._
import logging.OptionValWithLog
import memory.Memory
import logging.serverReporter.Error
import logging.serverReporter._
import shared.SourceCodeSubmissionResult
import stringModification.StringModificationProcessor.TupleSelectOrCaseClassSelect
import webDSL.webDescription.Register

import scala.reflect.runtime.universe
import scala.reflect.api

/**
  * Created by dupriez on 3/10/16.
  */
object ProgramEvaluator {
  val fullNameOfTheFunctionToEvaluate = "Main.main"
  val fullNameOfTheWebPageClass = "webDSL.webDescription.WebPage"

  def evaluateAndConvertResult(program: Program, sourceCode: String, serverReporter: ServerReporter): (Option[(WebPageWithIDedWebElements, SourceMap)], String) = {
    val sReporter = serverReporter.startProcess("ProgramEvaluator")
    val resultWebPage: Option[(WebPageWithIDedWebElements, SourceMap)] = evaluateProgram(program, sReporter) match {
      case Some((resultEvaluatedExpr, resultEvaluationTreeExpr)) => convertWebPageExprToClientWebPageAndSourceMap(resultEvaluatedExpr, resultEvaluationTreeExpr, program, sourceCode, sReporter)
      case None => None
    }
    //TODO: give something else than "" (the actual log of the evaluation/conversion process for example)
    resultWebPage match {
      case Some((webPageWithIDedWebElements, sourceMap)) =>
        (Some((webPageWithIDedWebElements, sourceMap)),"")
      case None =>
        (None, "")
    }
  }

  /**
    *
    * @param program
    * @param serverReporter
    * @return a couple of Expr: (resultEvaluatedExpr, resultEvaluationTreeExpr)
    */
  private def evaluateProgram(program: Program, serverReporter: ServerReporter): Option[(Expr, Expr)] = {
    val sReporter = serverReporter.startFunction("Evaluating Program with leon's Abstract Evaluator")
    val leonReporter = new DefaultReporter(Set())
    val ctx = leon.Main.processOptions(Seq()).copy(reporter = leonReporter)
    ctx.interruptManager.registerSignalHandler()
    val abstractEvaluator = new AbstractEvaluator(ctx, program)
    val mainFunDef = program.lookupFunDef(fullNameOfTheFunctionToEvaluate) match {
      case Some(funDef) => funDef
      case None => {
        sReporter.report(Error, "lookupFunDef(\"" + fullNameOfTheFunctionToEvaluate + "\") gave no result")
        return None
      }
    }
    abstractEvaluator.eval(FunctionInvocation(mainFunDef.typed, List())) match {
      case EvaluationResults.Successful((resultEvaluatedExpr, resultEvaluationTreeExpr)) => {
//        Note: in resultEvaluationTreeExpr, the function calls are replaced by their return value
        sReporter.report(Info, "Evaluation successful")
        //TODO: should also export the evaluationTreeExpr
        //sReporter.report(Info, "Expr of the evaluated webPage: "+ resultEvaluatedExpr)
        //sReporter.report(Info, "Expr of the unevaluated webPage: "+ resultEvaluationTreeExpr)
        Some((resultEvaluatedExpr, resultEvaluationTreeExpr))
      }
      case EvaluationResults.EvaluatorError(msg) => {
        sReporter.report(Error, "Evaluation failed: abstractEvaluator returned an EvaluationError with message: "+msg)
        None
      }
      case EvaluationResults.RuntimeError(msg) => {
        sReporter.report(Error, "Evaluation failed: abstractEvaluator returned a RuntimeError with message: "+msg)
        None
      }
    }
  }

  private def convertWebPageExprToClientWebPageAndSourceMap(webPageEvaluatedExpr: Expr, webPageEvaluationTreeExpr: Expr, program: Program, sourceCode: String, serverReporter: ServerReporter): Option[(WebPageWithIDedWebElements, SourceMap)] = {

    val sReporter = serverReporter.startFunction("Converting the WebPage Expr into a WebPage, and building the sourceMap")
    case class ExceptionDuringConversion(msg:String) extends Exception

    //sReporter.report(Info, "webPage expr to be converted: "+ webPageEvaluatedExpr)

    val result: Either[(WebPageWithIDedWebElements, SourceMap), String] = try {
      /** Looking up the case classes of webDSL_Leon**/
      def lookupCaseClass(program: Program)(caseClassFullName: String): CaseClassDef = {
        program.lookupCaseClass(caseClassFullName) match {
          case Some(classDef) => classDef
          case None => {
            val msg = "Conversion failed, lookupCaseClass(\"" + caseClassFullName + "\") gave no result"
            sReporter.report(Error, msg)
            throw ExceptionDuringConversion(msg)
          }
        }
      }

      // Maps CaseClassDefs to an associated reflect constructor.
      val constructorMap = WebDescriptionClassesRegister.fullNameToConstructorMap.map({case (fullName, constructor) => (lookupCaseClass(program)(fullName), constructor)})

      def unExpr(sReporter: ServerReporter)(e: Expr): Any = {
        //sReporter.report(Info, "Unexpring " + e)
        e match {
          case CaseClass(CaseClassType(caseClassDef, targs), args) => {
            constructorMap.get(caseClassDef) match {
              case Some((constructor, isWebElement)) =>
                val unexpredThing = constructor(args.map (unExpr(sReporter)): _*)
//                if (unexpredThing.isInstanceOf[WebElement]) {
//                  protoSourceMap.addMapping(new WebElementWrap(unexpredThing.asInstanceOf[WebElement]), e)
                unexpredThing
              case None =>
                val msg = s"""
                             |Looked for ${caseClassDef.toString} in the constructorMap, but did not find anything. Throwing exception.
                             |   Maybe ${caseClassDef.toString} is not registered in server/app/programEvaluator/WebDescriptionClassesRegister.
                  """.stripMargin
                throw ExceptionDuringConversion(msg)
            }
          }
          case l: Literal[_] => l.value
//            unapply magic
          case TupleSelectOrCaseClassSelect(actualExpr) => unExpr(sReporter)(actualExpr)
          case _ =>
//            unExpr(sReporter)(stringModification.StringModificationProcessor.simplifyCaseSelect(e))
            sReporter.report(Info, "Unexpr default case, something is probably wrong")
        }
      }

      def buildSourceMapAndGiveIDsToWebElements(webPage: WebPage, resultEvaluationTreeExpr: Expr, sourceCode: String, program: Program, serverReporter: ServerReporter): (WebPageWithIDedWebElements, SourceMap) = {
        val sReporter = serverReporter.startFunction("buildSourceMapAndGiveIDsToWebElements")
        val sourceMap = new SourceMap(sourceCode, program)
        val bootstrapWebElementLeonList: leon.collection.List[WebElement] = webPage match {
          case WebPage(webPAttr, sons) => sons
        }
        def getCaseClassDefValOrFail(optionValWithLog: OptionValWithLog[CaseClassDef]) : CaseClassDef = {
          optionValWithLog match{
            case OptionValWithLog(Some(value), log) => value
            case OptionValWithLog(None, log) =>
              throw ExceptionDuringConversion(log)
          }
        }
        val webPageCaseClassDef = getCaseClassDefValOrFail(sourceMap.webPage_webElementCaseClassDef(sReporter))
        val paragraphCaseClassDef = getCaseClassDefValOrFail(sourceMap.paragraph_webElementCaseClassDef(sReporter))
        val headerCaseClassDef = getCaseClassDefValOrFail(sourceMap.header_webElementCaseClassDef(sReporter))
        val inputCaseClassDef = getCaseClassDefValOrFail(sourceMap.input_webElementCaseClassDef(sReporter))
        val divCaseClassDef = getCaseClassDefValOrFail(sourceMap.div_webElementCaseClassDef(sReporter))
        val consCaseClassDef = getCaseClassDefValOrFail(sourceMap.leonCons_caseClassDef(sReporter))
        val nilCaseClassDef = getCaseClassDefValOrFail(sourceMap.leonNil_caseClassDef(sReporter))
//        println("paragraph caseClassDef: " + paragraphCaseClassDef)
//        println("div caseClassDef" + divCaseClassDef)
//        println("cons caseClassDef" + consCaseClassDef)
//        println("nil caseClassDef" + nilCaseClassDef)

        def exprOfLeonListOfExprToLeonListOfExpr(leonListExpr: Expr) : leon.collection.List[Expr] = {
          val actualLeonListExpr = TupleSelectAndCaseClassSelectRemover.removeTopLevelTupleSelectsAndCaseClassSelects(leonListExpr)
          actualLeonListExpr match {
            case CaseClass(CaseClassType(`consCaseClassDef`, targs), args) =>
              args match {
                case List(elem, remainingList) => leon.collection.List(elem) ++ exprOfLeonListOfExprToLeonListOfExpr(remainingList)
              }
            case CaseClass(CaseClassType(`nilCaseClassDef`, targs), args) => leon.collection.List()
          }
        }
        val bootstrapExprOfUnevaluatedWebElementLeonList : leon.collection.List[Expr] = resultEvaluationTreeExpr match {
          case CaseClass(CaseClassType(`webPageCaseClassDef`, targs_1), args) =>
            args match {
              case List(webPageAttributeListExpr, webPageSonsListExpr) =>
                exprOfLeonListOfExprToLeonListOfExpr(webPageSonsListExpr)
            }
        }
        def leonListToList[T](leonList: leon.collection.List[T]): List[T] = {
          val listBuffer = leonList.foldLeft(scala.collection.mutable.ListBuffer[T]())((list, elem)=>list += elem)
          listBuffer.toList
        }
        var counterID = 0
        def generateID() = {counterID+=1; counterID}
        /**
          * Traverse webElement and the correspondingUnevaluated Expr at the same time.
          * Creates a tree corresponding to webElement, but made of WebElementWithID.
          * Add the mappings Id -> UnevaluatedExpr to sourceMap
          *
          * @param sourceMap
          * @param webElement
          * @param correspondingUnevaluatedExpr
          * @return
          */
        def giveIDToWebElementsAndFillSourceMap(sourceMap: SourceMap, sReporter: ServerReporter)(webElement: WebElement, correspondingUnevaluatedExpr: Expr) : WebElementWithID = {
          //sReporter.report(Info, "Processing: webElement: "+webElement+" and corresponding unevaluated Expr: "+correspondingUnevaluatedExpr)
          def sanityCheck(webElement: WebElement, correspondingUnevaluatedExpr: Expr, caseClassDef: CaseClassDef, webElementName: String, sReporter:ServerReporter): Unit = {
            correspondingUnevaluatedExpr match {
              case CaseClass(CaseClassType(`caseClassDef`, targs), args) => ()/*correspondingUnevaluatedExpr*/
//              case TupleSelectOrCaseClassSelect(actualExpr)=>
//                sanityCheck(webElement, actualExpr, caseClassDef, webElementName, sReporter)
              case _ =>
                sReporter.report(Error,
                s"""When IDing the webElements and building the sourceMap, function giveIDToWebElementsAndFillSourceMap was given a $webElementName and an expr that did not represent a $webElementName:
                    |   $webElementName: $webElement
                    |   Expr: $correspondingUnevaluatedExpr
                  """.stripMargin)
//                TODO: throw an exception instead of the following line
//                correspondingUnevaluatedExpr
            }
          }
          val actualCorrespondingUnevaluatedExpr = TupleSelectAndCaseClassSelectRemover.removeTopLevelTupleSelectsAndCaseClassSelects(correspondingUnevaluatedExpr)
          webElement match {
            // Wildcard patterns are not used for most of the following cases, so that the compiler complains whenever
            // the types of the arguments of these case classes are changed (in their definition).
            // Because the following code may go haywire if these types are changed (especially if WebElements are added
            // to the definitions of these case classes)
            case WebElementWithID(_,_) =>
//              Should never happen
              sReporter.report(Error,
                s"""Something went wrong, function giveIDToWebElementsAndFillSourceMap was given a WebElementWithID:
                    |   WebElementWithID: $webElement
                    |   Expr: $actualCorrespondingUnevaluatedExpr
                  """.stripMargin)
              WebElementWithID(webElement, 0)
            case Paragraph(text: String) =>
              sanityCheck(webElement, actualCorrespondingUnevaluatedExpr, paragraphCaseClassDef, "Paragraph", sReporter)
              val id = generateID()
              sourceMap.addMapping(id, webElement, actualCorrespondingUnevaluatedExpr)
              WebElementWithID(webElement, id)
            case Header(text: String, level: HeaderLevel) =>
              sanityCheck(webElement, actualCorrespondingUnevaluatedExpr, headerCaseClassDef, "Header", sReporter)
              val id = generateID()
              sourceMap.addMapping(id, webElement, actualCorrespondingUnevaluatedExpr)
              WebElementWithID(webElement, id)
            case Input(tpe, placeHolder, text) =>
              sanityCheck(webElement, actualCorrespondingUnevaluatedExpr, inputCaseClassDef, "Input", sReporter)
              val id = generateID()
              sourceMap.addMapping(id, webElement, actualCorrespondingUnevaluatedExpr)
              WebElementWithID(webElement, id)
            case Div(sons: leon.collection.List[WebElement]) =>
              sanityCheck(webElement, actualCorrespondingUnevaluatedExpr, divCaseClassDef, "Div", sReporter)
              actualCorrespondingUnevaluatedExpr match {
                case CaseClass(CaseClassType(`divCaseClassDef`, targs), args) => {
                  val id = generateID()
                  sourceMap.addMapping(id, webElement, actualCorrespondingUnevaluatedExpr)
                  val sonsWebElemCorrespUnevalExprCouplLeonList = sons.zip(exprOfLeonListOfExprToLeonListOfExpr(args(0)))
                  val iDedSons : leon.collection.List[WebElement]= sonsWebElemCorrespUnevalExprCouplLeonList.map(
                    {case (webElem, correspUnevalExpr) => giveIDToWebElementsAndFillSourceMap(sourceMap, sReporter)(webElem, correspUnevalExpr)}
                  )
                  WebElementWithID(Div(iDedSons), id)
                }
              }
          }
        }
        def scalaListToLeonList[T](input: List[T], resultUnderConstruction: leon.collection.List[T] = leon.collection.List[T]()) : leon.collection.List[T]= {
          input match {
            case head :: tail =>
              scalaListToLeonList[T](tail, Cons[T](head, resultUnderConstruction))
            case List() =>
              resultUnderConstruction.reverse
          }
        }
        assert(bootstrapWebElementLeonList.size == bootstrapExprOfUnevaluatedWebElementLeonList.size)
        val bootstrapList : leon.collection.List[(WebElement, Expr)]= bootstrapWebElementLeonList.zip[Expr](bootstrapExprOfUnevaluatedWebElementLeonList)
        val webPageWithIDedElements = WebPageWithIDedWebElements(
          webPage.webPageAttributes,
          bootstrapList.map[WebElementWithID]( {case (webElem, expr) => giveIDToWebElementsAndFillSourceMap(sourceMap, sReporter)(webElem, expr)})
        )
        (webPageWithIDedElements, sourceMap)
      }

      //WebPage without the contained WebElement having proper IDs
      val webPage = unExpr(sReporter.startFunction("Unexpring WebPage Expr: "+webPageEvaluatedExpr))(webPageEvaluatedExpr).asInstanceOf[WebPage]
      val (webPageWithIDedWebElements, sourceMap) = buildSourceMapAndGiveIDsToWebElements(webPage, webPageEvaluationTreeExpr, sourceCode, program, sReporter)
      sReporter.report(Info, "WebPageWithIDedWebElements: " + webPageWithIDedWebElements.toString)
//      val d =  WebPageWithIDedWebElements(Nil(),Cons(WebElementWithID(Header(HeAdEr,HLTwo()),1),Cons(WebElementWithID(Paragraph(text),2),Cons(WebElementWithID(Div(Cons(Paragraph(text2),Nil())),3),Nil()))))

      val programEvaluationResult = (webPageWithIDedWebElements, sourceMap)
      Memory.sourceMap = sourceMap
      sReporter.report(Info, "Program evaluation result after unExpr: " + programEvaluationResult)
      Left(programEvaluationResult)
    }
    catch {
      case ExceptionDuringConversion(errorString) => {
        Right(errorString)
      }
    }
    result match {
      case Left((webPageWithIDedWebElements, sourceMap)) =>
        sReporter.report(Info, "Conversion and SourceMap building successful")
        Some((webPageWithIDedWebElements, sourceMap))
      case Right(errorString) =>
        sReporter.report(Error, "Conversion and SourceMap building failed: " + errorString)
        None
    }
  }
}