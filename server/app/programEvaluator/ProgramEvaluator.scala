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
import memory.Memory
import logging.serverReporter.Error
import logging.serverReporter._
import shared.{IDGenerator, SourceCodeSubmissionResult}
import webDSL.webDescription.Register

import scala.reflect.runtime.universe
import scala.reflect.api

/**
  * Created by dupriez on 3/10/16.
  */
object ProgramEvaluator {
  val fullNameOfTheFunctionToEvaluate = "Main.main"
  val fullNameOfTheWebPageClass = "webDSL.webDescription.WebPage"

  def evaluateAndConvertResult(program: Program, sourceCode: String, sReporter: ServerReporter): (Option[(WebPageWithIDedWebElements, SourceMap)], String) = {
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
    * @param sReporter
    * @return a couple of Expr: (resultEvaluatedExpr, resultEvaluationTreeExpr)
    */
  private def evaluateProgram(program: Program, sReporter: ServerReporter): Option[(Expr, Expr)] = {
    sReporter.report(Info, "Starting evaluation of the submitted program...")
    val leonReporter = new DefaultReporter(Set())
    val ctx = leon.Main.processOptions(Seq()).copy(reporter = leonReporter)
    ctx.interruptManager.registerSignalHandler()
    val abstractEvaluator = new AbstractEvaluator(ctx, program)
    val mainFunDef = program.lookupFunDef(fullNameOfTheFunctionToEvaluate) match {
      case Some(funDef) => funDef
      case None => {
        sReporter.report(Error, "lookupFunDef(\"" + fullNameOfTheFunctionToEvaluate + "\") gave no result", 1)
        return None
      }
    }
    abstractEvaluator.eval(FunctionInvocation(mainFunDef.typed, List())) match {
      case EvaluationResults.Successful((resultEvaluatedExpr, resultEvaluationTreeExpr)) => {
//        Note: in resultEvaluationTreeExpr, the function calls are replaced by their return value
        sReporter.report(Info, "Evaluation successful.", 1)
        //TODO: should also export the evaluationTreeExpr
        sReporter.report(Info, "resultEvaluatedExpr: "+ resultEvaluatedExpr, 1)
        sReporter.report(Info, "resultEvaluationTreeExpr: "+ resultEvaluationTreeExpr, 1)
        return Some((resultEvaluatedExpr, resultEvaluationTreeExpr))
      }
      case EvaluationResults.EvaluatorError(msg) => {
        sReporter.report(Error, "Evaluation failed: abstractEvaluator returned an EvaluationError: "+msg, 1)
        return None
      }
      case EvaluationResults.RuntimeError(msg) => {
        sReporter.report(Error, "Evaluation failed: abstractEvaluator returned a RuntimeError: "+msg, 1)
        return None
      }
    }
  }

  private def convertWebPageExprToClientWebPageAndSourceMap(webPageEvaluatedExpr: Expr, webPageEvaluationTreeExpr: Expr, program: Program, sourceCode: String, sReporter: ServerReporter): Option[(WebPageWithIDedWebElements, SourceMap)] = {


    case class ExceptionDuringConversion(msg:String) extends Exception

    sReporter.report(Info, "Starting conversion of the leon Expr returned by the submitted program to a WebPage...")
    sReporter.report(Info, "webPageExpr to be converted: "+ webPageEvaluatedExpr, 1)

    val result: Either[(WebPageWithIDedWebElements, SourceMap), String] = try {
      /** Looking up the case classes of webDSL_Leon**/
      def lookupCaseClass(program: Program)(caseClassFullName: String): CaseClassDef = {
        program.lookupCaseClass(caseClassFullName) match {
          case Some(classDef) => classDef
          case None => {
            val msg = "Conversion failed, lookupCaseClass(\"" + caseClassFullName + "\") gave no result"
            sReporter.report(Error, msg, 1)
            throw ExceptionDuringConversion(msg)
          }
        }
      }
//      val webPageClassDef = lookupCaseClass(program)(fullNameOfTheWebPageClass)

      //Only actual case classes, not the traits present in the hierarchy
//      val fullClassNamesList = List(
//        "leon.webDSL.webDescription.WebPage",
//        "leon.webDSL.webDescription.TestWebAttribute1",
//        "leon.webDSL.webDescription.TestWebElement1",
//        "leon.webDSL.webDescription.TestWebElement2",
//        "leon.webDSL.webDescription.TestWebPageAttribute1"
//      )

//      val caseClassDefList = fullClassNamesList.map(lookupCaseClass(program))

//      val map = Map("leon.webDSL.webDescription.WebPage" -> WebPage.getClass)

//      def getReflectConstructor[T: universe.TypeTag] = {
//        sReporter.report(Info, "getReflectConstructor was called")
//        val mirror = universe.runtimeMirror(getClass.getClassLoader)
//        val classs = universe.typeOf[T].typeSymbol.asClass
//        val classMirror = mirror.reflectClass(classs)
//        val constructor = universe.typeOf[T].decl(universe.termNames.CONSTRUCTOR).asMethod
//        val constructorMirror = classMirror.reflectConstructor(constructor)
//        constructorMirror
//      }

//      def backward[T](tpe: Type): TypeTag[T] =
//        val mirror = universe.runtimeMirror(getClass.getClassLoader)
//        TypeTag(mirror, new api.TypeCreator {
//          def apply[U <: api.Universe with Singleton](m: api.Mirror[U]) =
//            if (m eq mirror) tpe.asInstanceOf[U # Type]
//            else throw new IllegalArgumentException(s"Type tag defined in $mirror cannot be migrated to other mirrors.")
//        })

      //The second number is the number of arguments the constructor requires
//      val constructorMap : Map[CaseClassDef, universe.MethodMirror] = Map(
//        (lookupCaseClass(program)("leon.webDSL.webDescription.WebPage"), getReflectConstructor[WebPage]),
//        (lookupCaseClass(program)("leon.webDSL.webDescription.TestWebElement2"), getReflectConstructor[TestWebElement2]),
//        (lookupCaseClass(program)("leon.collection.Cons"), getReflectConstructor[leon.collection.Cons[_]]),
//        (lookupCaseClass(program)("leon.collection.Nil"), getReflectConstructor[leon.collection.Nil[_]])
//      )

      val constructorMap = WebDescriptionClassesRegister.fullNameToConstructorMap.map({case (fullName, constructor) => (lookupCaseClass(program)(fullName), constructor)})

//      def getTypeFromClassName(className: String): reflect.runtime.universe.Type = {
//        val mirror = universe.runtimeMirror(getClass.getClassLoader)
//        val class_ = Class.forName(className)
//        val classSymbol = mirror.classSymbol(class_)
//        val type_ : universe.Type = classSymbol.toType
//        val c =getReflectConstructor(type_.asInstanceOf[universe.TypeTag])
//        val classs = universe.typeOf(type_).typeSymbol.asClass
//        type_
//      }

//      val consDef = lookupCaseClass(program)("leon.collection.Cons")
//      val nilDef = lookupCaseClass(program)("leon.collection.Nil")

      def unExpr(e: Expr): Any = {
//        sReporter.report(Info, "unExpring: "+e, 1)
//        e match {
//          case StringLiteral(string) =>
//            sReporter.report(Info, "StringLiteral position: "+e.getPos, 1)
//          case _ => ()
//        }
        e match {
          case CaseClass(CaseClassType(caseClassDef, targs), args) => {
            constructorMap.get(caseClassDef) match {
              case Some((constructor, isWebElement)) =>
                val unexpredThing = constructor(args.map (unExpr): _*)
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
          case _ => {sReporter.report(Info, "Default case, the expr was: "+ e, 1)}
        }
      }

      def buildSourceMapAndGiveIDsToWebElements(webPage: WebPage, resultEvaluationTreeExpr: Expr, sourceCode: String): (WebPageWithIDedWebElements, SourceMap) = {
        val sourceMap = new SourceMap(sourceCode)
        val bootstrapWebElementLeonList: leon.collection.List[WebElement] = webPage match {
          case WebPage(webPAttr, sons) => sons
        }
        val webPageCaseClassDef = lookupCaseClass(program)("leon.webDSL.webDescription.WebPage")
        val paragraphCaseClassDef = lookupCaseClass(program)("leon.webDSL.webDescription.Paragraph")
        val headerCaseClassDef = lookupCaseClass(program)("leon.webDSL.webDescription.Header")
        val divCaseClassDef = lookupCaseClass(program)("leon.webDSL.webDescription.Div")
        val consCaseClassDef = lookupCaseClass(program)("leon.collection.Cons")
        val nilCaseClassDef = lookupCaseClass(program)("leon.collection.Nil")
//        println("paragraph caseClassDef: " + paragraphCaseClassDef)
//        println("div caseClassDef" + divCaseClassDef)
//        println("cons caseClassDef" + consCaseClassDef)
//        println("nil caseClassDef" + nilCaseClassDef)
        def exprOfLeonListOfExprToLeonListOfExpr(leonListExpr: Expr) : leon.collection.List[Expr] = {
          leonListExpr match {
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
        def giveIDToWebElementsAndFillSourceMap(sourceMap: SourceMap)(webElement: WebElement, correspondingUnevaluatedExpr: Expr) : WebElementWithID = {
          def sanityCheck(webElement: WebElement, correspondingUnevaluatedExpr: Expr, caseClassDef: CaseClassDef, webElementName: String, sReporter:ServerReporter) = {
            correspondingUnevaluatedExpr match {
              case CaseClass(CaseClassType(`caseClassDef`, targs), args) => ()
              case _ => sReporter.report(Error,
                s"""When IDing the webElements and building the sourceMap, function giveIDToWebElementsAndFillSourceMap was given a $webElementName and an expr that did not represent a $webElementName:
                    |   $webElementName: $webElement
                    |   Expr: $correspondingUnevaluatedExpr
                  """.stripMargin)
            }
          }
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
                    |   Expr: $correspondingUnevaluatedExpr
                  """.stripMargin)
              WebElementWithID(webElement, 0)
            case Paragraph(text: String) =>
              sanityCheck(webElement, correspondingUnevaluatedExpr, paragraphCaseClassDef, "Paragraph", sReporter)
              val id = generateID()
              sourceMap.addMapping(id, correspondingUnevaluatedExpr)
              WebElementWithID(webElement, id)
            case Header(text: String, level: HeaderLevel) =>
              sanityCheck(webElement, correspondingUnevaluatedExpr, headerCaseClassDef, "Header", sReporter)
              val id = generateID()
              sourceMap.addMapping(id, correspondingUnevaluatedExpr)
              WebElementWithID(webElement, id)
            case Div(sons: leon.collection.List[WebElement]) =>
              sanityCheck(webElement, correspondingUnevaluatedExpr, divCaseClassDef, "Div", sReporter)
              correspondingUnevaluatedExpr match {
                case CaseClass(CaseClassType(`divCaseClassDef`, targs), args) => {
                  val id = generateID()
                  sourceMap.addMapping(id, correspondingUnevaluatedExpr)
                  val sonsWebElemCorrespUnevalExprCouplLeonList = sons.zip(exprOfLeonListOfExprToLeonListOfExpr(args(0)))
//                  val listForRecursion = leonListToList[WebElement](sons).zip(args.toList)
                  println("sonsWebElemCorrespUnevalExprCouplLeonList: " + sonsWebElemCorrespUnevalExprCouplLeonList)
                  println("args of div: " + args)
//                  Actually, iDedSons is a leonList of WebElementWithID
                  val iDedSons : leon.collection.List[WebElement]= sonsWebElemCorrespUnevalExprCouplLeonList.map(
                    {case (webElem, correspUnevalExpr) => giveIDToWebElementsAndFillSourceMap(sourceMap)(webElem, correspUnevalExpr)}
//                    (webElemAndCorrespUnevalExpr) => {
//                      webElemAndCorrespUnevalExpr match {
//                        case(webElem, correspUnevalExpr) =>
//                          giveIDToWebElementsAndFillSourceMap(sourceMap)(webElem, correspUnevalExpr)
//                      }
//                    }
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
          bootstrapList.map[WebElementWithID]( {case (webElem, expr) => giveIDToWebElementsAndFillSourceMap(sourceMap)(webElem, expr)})
        )
        (webPageWithIDedElements, sourceMap)
      }

      //WebPage without the contained WebElement having proper IDs
      val webPage = unExpr(webPageEvaluatedExpr).asInstanceOf[WebPage]
      //Uses side effects to give an ID to each webElement of a leonList
//      def giveIDToWebElements(webElList: leon.collection.List[WebElement], idGenerator: IDGenerator): Unit = {
//        webElList.foldLeft(0)((useless, webElem) => {
//          webElem.weid = idGenerator.generateID()
//          giveIDToWebElements(webElem.sons, idGenerator)
//          0
//        })
//      }
//      giveIDToWebElements(webPage.sons, new IDGenerator)
//      webPage.sons.foldLeft(0)((useless, webEleme) => {println(webEleme.weid); useless})
      val (webPageWithIDedWebElements, sourceMap) = buildSourceMapAndGiveIDsToWebElements(webPage, webPageEvaluationTreeExpr, sourceCode)
      sReporter.report(Info, "WebPageWithIDedWebElements:" + webPageWithIDedWebElements.toString, 1)
//      val d =  WebPageWithIDedWebElements(Nil(),Cons(WebElementWithID(Header(HeAdEr,HLTwo()),1),Cons(WebElementWithID(Paragraph(text),2),Cons(WebElementWithID(Div(Cons(Paragraph(text2),Nil())),3),Nil()))))

      val programEvaluationResult = (webPageWithIDedWebElements, sourceMap)
      Memory.sourceMap = sourceMap
      sReporter.report(Info, "Program evaluation result after unExpr: " + programEvaluationResult,1)
      Left(programEvaluationResult)
    }
    catch {
      case ExceptionDuringConversion(errorString) => {
        Right(errorString)
      }
    }
    result match {
      case Left((webPageWithIDedWebElements, sourceMap)) =>
        sReporter.report(Info, "Conversion successful", 1)
        Some((webPageWithIDedWebElements, sourceMap))
      case Right(errorString) =>
        sReporter.report(Error, "Conversion failed: " + errorString, 1)
        None
    }
  }
}