package programEvaluator

import java.lang.reflect.Type
import javassist.bytecode.stackmap.TypeTag

import leon.DefaultReporter
import leon.evaluators.{EvaluationResults, AbstractEvaluator}
import leon.purescala.Definitions.{CaseClassDef, Program}
import leon.purescala.Expressions._
import leon.purescala.Types.CaseClassType
import leon.webDSL.webDescription._
import serverReporter.Error
import serverReporter._
import shared.SourceCodeSubmissionResult
import webDSL.webDescription.Register
import scala.reflect.runtime.universe
import scala.reflect.api

/**
  * Created by dupriez on 3/10/16.
  */
object ProgramEvaluator {
  val fullNameOfTheFunctionToEvaluate = "Main.main"
  val fullNameOfTheWebPageClass = "webDSL.webDescription.WebPage"

  def evaluateAndConvertResult(program: Program, sourceCode: String, sReporter: ServerReporter): (Option[(WebPage, SourceMap)], String) = {
    val resultWebPage: Option[(WebPage, SourceMap)] = evaluateProgram(program, sReporter) match {
      case Some(resultExpr) => convertWebPageExprToClientWebPageAndSourceMap(resultExpr, program, sourceCode, sReporter)
      case None => None
    }
    //TODO: give something else than "" (the actual log of the evaluation/conversion process for example)
    resultWebPage match {
      case Some((webPage, sourceMap)) =>
        (Some((webPage, sourceMap)),"")
      case None =>
        (None, "")
    }
  }

  private def evaluateProgram(program: Program, sReporter: ServerReporter): Option[Expr] = {
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
      case EvaluationResults.Successful((resultExpr, evaluationTreeExpr)) => {
        sReporter.report(Info, "Evaluation successful.", 1)
        //TODO: should also export the evaluationTreeExpr
        sReporter.report(Info, "resultExpr: "+ resultExpr, 1)
        sReporter.report(Info, "evaluationTreeExpr: "+ evaluationTreeExpr, 1)
        return Some(resultExpr)
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

  private def convertWebPageExprToClientWebPageAndSourceMap(webPageExpr: Expr, program: Program, sourceCode: String, sReporter: ServerReporter): Option[(WebPage, SourceMap)] = {


    case class ExceptionDuringConversion(msg:String) extends Exception

    sReporter.report(Info, "Starting conversion of the leon Expr returned by the submitted program to a WebPage...")
    sReporter.report(Info, "webPageExpr to be converted: "+ webPageExpr, 1)

    val result: Either[(WebPage, SourceMap), String] = try {
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

      def unExpr(sourceMap: SourceMap)(e: Expr): Any = {
//        sReporter.report(Info, "unExpr was called")
        e match {
          case CaseClass(CaseClassType(caseClassDef, targs), args) => {
            constructorMap.get(caseClassDef) match {
              case Some(constructor) => constructor(args.map (unExpr(sourceMap)): _*)
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

      val sourceMap = new SourceMap(sourceCode)
      //WebPage without the contained WebElement having proper IDs
      val webPage = unExpr(sourceMap)(webPageExpr).asInstanceOf[WebPage]
      //Uses side effects to give an ID to each webElement of a leonList
      def giveIDToWebElements(webElList: leon.collection.List[WebElement], idGenerator: IDGenerator): Unit = {
        webElList.foldLeft(0)((useless, webElem) => {
          webElem.weid = idGenerator.generateID()
          giveIDToWebElements(webElem.sons, idGenerator)
          0
        })
      }
      giveIDToWebElements(webPage.sons, new IDGenerator)
      webPage.sons.foldLeft(0)((useless, webEleme) => {println(webEleme.weid); useless})
      val programEvaluationResult = (webPage, sourceMap)
      sReporter.report(Info, "Program evaluation result after unExpr: " + programEvaluationResult,1)
      Left(programEvaluationResult)
    }
    catch {
      case ExceptionDuringConversion(errorString) => {
        Right(errorString)
      }
    }
    result match {
      case Left((webPage, sourceMap)) =>
        sReporter.report(Info, "Conversion successful", 1)
        Some((webPage, sourceMap))
      case Right(errorString) =>
        sReporter.report(Error, "Conversion failed: " + errorString, 1)
        None
    }
  }
}

class IDGenerator {
  private var _counter = 0
  def generateID() = {
    _counter = _counter+1
    _counter
  }
  def reset() = {
    _counter = 0
  }
}
