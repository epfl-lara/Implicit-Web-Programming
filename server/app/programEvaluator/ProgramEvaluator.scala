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

import scala.reflect.runtime.universe
import scala.reflect.api

/**
  * Created by dupriez on 3/10/16.
  */
object ProgramEvaluator {
  val fullNameOfTheFunctionToEvaluate = "Main.main"
  val fullNameOfTheWebPageClass = "webDSL.webDescription.WebPage"

  case class ExceptionDuringConversion(msg:String) extends Exception
  
  class ConversionBuilder(sourceMap: SourceMap, sReporter: ServerReporter) {
    private def getCaseClassDefValOrFail(optionValWithLog: OptionValWithLog[CaseClassDef]) : CaseClassDef = {
      optionValWithLog match{
        case OptionValWithLog(Some(value), log) => value
        case OptionValWithLog(None, log) =>
          throw ExceptionDuringConversion(log)
      }
    }
    val webSiteCaseClassDef = getCaseClassDefValOrFail(sourceMap.webSite_caseClassDef(sReporter))
    val webPageCaseClassDef = getCaseClassDefValOrFail(sourceMap.webPage_caseClassDef(sReporter))
    val textElementCaseClassDef = getCaseClassDefValOrFail(sourceMap.textElement_webElementCaseClassDef(sReporter))
    val elementCaseClassDef = getCaseClassDefValOrFail(sourceMap.element_webElementCaseClassDef(sReporter))
    val webPropertyCaseClassDef = getCaseClassDefValOrFail(sourceMap.webAttribute_webElementCaseClassDef(sReporter))
    val webStyleCaseClassDef = getCaseClassDefValOrFail(sourceMap.webStyle_webElementCaseClassDef(sReporter))
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
        case _ => throw new Exception("Did not match leon list expr")
      }
    }
  }

  
  def evaluateAndConvertResult(program: Program, sourceCode: String, serverReporter: ServerReporter): (Option[(WebSiteWithIDedContent, SourceMap)], String) = {
    val sReporter = serverReporter.startProcess("ProgramEvaluator")
    val resultWebSite: Option[(WebSiteWithIDedContent, SourceMap)] = evaluateProgram(program, sReporter) match {
      case Some((resultEvaluatedExpr, resultEvaluationTreeExpr)) => convertWebSiteExprToClientWebSiteAndSourceMap(resultEvaluatedExpr, resultEvaluationTreeExpr, program, sourceCode, sReporter)
      case None => None
    }
    //TODO: give something else than "" (the actual log of the evaluation/conversion process for example)
    resultWebSite match {
      case Some((webSiteWithIDedContent, sourceMap)) =>
        (Some((webSiteWithIDedContent, sourceMap)),"")
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

  private def convertWebSiteExprToClientWebSiteAndSourceMap(webSiteEvaluatedExpr: Expr, webSiteEvaluationTreeExpr: Expr, program: Program, sourceCode: String, serverReporter: ServerReporter): Option[(WebSiteWithIDedContent, SourceMap)] = {

    val sReporter = serverReporter.startFunction("Converting the WebSite Expr into a WebSite, and building the sourceMap")
    

    //sReporter.report(Info, "webSite expr to be converted: "+ webSiteEvaluatedExpr)

    val result: Either[(WebSiteWithIDedContent, SourceMap), String] = try {
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
        val actualExpr = TupleSelectAndCaseClassSelectRemover.removeTopLevelTupleSelectsAndCaseClassSelects(e)
        actualExpr match {
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
//          case TupleSelectOrCaseClassSelect(actualExpr) => unExpr(sReporter)(actualExpr)
          case _ =>
//            unExpr(sReporter)(stringModification.StringModificationProcessor.simplifyCaseSelect(e))
            sReporter.report(Info, "Unexpr default case, something is probably wrong")
        }
      }

      def buildSourceMapAndGiveIDsToWebElements(webSite: WebSite, resultEvaluationTreeExpr: Expr, sourceCode: String, program: Program, serverReporter: ServerReporter): (WebSiteWithIDedContent, SourceMap) = {
        val sReporter = serverReporter.startFunction("buildSourceMapAndGiveIDsToWebElements")
        val sourceMap = new SourceMap(sourceCode, program)
        def leonListToList[T](leonList: leon.collection.List[T]): List[T] = {
          val listBuffer = leonList.foldLeft(scala.collection.mutable.ListBuffer[T]())((list, elem)=>list += elem)
          listBuffer.toList
        }
        def scalaListToLeonList[T](input: List[T], resultUnderConstruction: leon.collection.List[T] = leon.collection.List[T]()) : leon.collection.List[T]= {
          input match {
            case head :: tail =>
              scalaListToLeonList[T](tail, Cons[T](head, resultUnderConstruction))
            case List() =>
              resultUnderConstruction.reverse
          }
        }
        val cb = new ConversionBuilder(sourceMap, serverReporter)
        import cb._
        var counterID = 0
        def generateID() = {counterID+=1; counterID}

        def processWebPage(webPage: WebPage, correspondingUnevaluatedExpr: Expr): WebPageWithIDedWebElements = {
          val bootstrapWebElement: WebElement = webPage match {
            case WebPage(elem) => elem
          }
          val bootstrapExprOfUnevaluatedWebElement : Expr = correspondingUnevaluatedExpr match {
            case CaseClass(CaseClassType(`webPageCaseClassDef`, targs_1), Seq(arg)) =>
              arg
          }

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
                sReporter.report(Info, "#4")
  //              Should never happen
                sReporter.report(Error,
                  s"""Something went wrong, function giveIDToWebElementsAndFillSourceMap was given a WebElementWithID:
                      |   WebElementWithID: $webElement
                      |   Expr: $actualCorrespondingUnevaluatedExpr
                    """.stripMargin)
                WebElementWithID(webElement, 0)
              case TextElement(text: String) =>
                sanityCheck(webElement, actualCorrespondingUnevaluatedExpr, textElementCaseClassDef, "TextElement", sReporter)
                val id = generateID()
                sourceMap.addMapping(id, webElement, actualCorrespondingUnevaluatedExpr)
                WebElementWithID(webElement, id)
              case Element(tag: String, sons, properties, styles) =>
                sanityCheck(webElement, actualCorrespondingUnevaluatedExpr, elementCaseClassDef, "Element", sReporter)
                actualCorrespondingUnevaluatedExpr match {
                  case CaseClass(CaseClassType(`elementCaseClassDef`, targs), List(argTag, argSons, argProperties, argStyles)) =>
                    val id = generateID()
                    sourceMap.addMapping(id, webElement, actualCorrespondingUnevaluatedExpr)
                    val sonsWebElemCorrespUnevalExprCouplLeonList = sons.zip(exprOfLeonListOfExprToLeonListOfExpr(argSons))
                    val iDedSons : leon.collection.List[WebElement]= sonsWebElemCorrespUnevalExprCouplLeonList.map(
                      {case (webElem, correspUnevalExpr) => giveIDToWebElementsAndFillSourceMap(sourceMap, sReporter)(webElem, correspUnevalExpr)}
                    )
                    WebElementWithID(Element(tag, iDedSons, properties, styles), id)
                  case e => throw new Exception("Did not pattern match Element")
                }
            }
          }
          val webPageWithIDedElements = WebPageWithIDedWebElements(
            //webPage.webPageAttributes,
            giveIDToWebElementsAndFillSourceMap(sourceMap, sReporter)(bootstrapWebElement, bootstrapExprOfUnevaluatedWebElement)
          )
          webPageWithIDedElements
//          (webPageWithIDedElements, sourceMap)
        }

        val listOfWebPageExpr = resultEvaluationTreeExpr match {
          case CaseClass(CaseClassType(`webSiteCaseClassDef`, targs_1), Seq(arg)) =>
            exprOfLeonListOfExprToLeonListOfExpr(arg)
        }
        val listOfWebPageWithIDedWebElement = webSite.main.zip[Expr](listOfWebPageExpr).map({case (webPage, webPageUnevaluatedExpr) => processWebPage(webPage, webPageUnevaluatedExpr)})
        (WebSiteWithIDedContent(listOfWebPageWithIDedWebElement), sourceMap)
      }

      //WebPage without the contained WebElement having proper IDs
      val webSite = unExpr(sReporter.startFunction("Unexpring WebPage Expr: "+webSiteEvaluatedExpr))(webSiteEvaluatedExpr).asInstanceOf[WebSite]
      val (webPageWithIDedWebElements, sourceMap) = buildSourceMapAndGiveIDsToWebElements(webSite, webSiteEvaluationTreeExpr, sourceCode, program, sReporter)
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
      case Left((webSiteWithIDedContent, sourceMap)) =>
        sReporter.report(Info, "Conversion and SourceMap building successful")
        Some((webSiteWithIDedContent, sourceMap))
      case Right(errorString) =>
        sReporter.report(Error, "Conversion and SourceMap building failed: " + errorString)
        None
    }
  }
}