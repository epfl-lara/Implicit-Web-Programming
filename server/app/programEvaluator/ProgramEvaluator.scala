package programEvaluator

import java.lang.reflect.Type
import javassist.bytecode.stackmap.TypeTag

import leon.DefaultReporter
import leon.evaluators.{EvaluationResults, AbstractEvaluator}
import leon.purescala.Definitions.{CaseClassDef, Program}
import leon.purescala.Expressions.{Literal, CaseClass, FunctionInvocation, Expr}
import leon.purescala.Types.CaseClassType
import leon.webDSL.webDescription._
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

  def evaluateAndConvertResult(program: Program, sReporter: ServerReporter): SourceCodeSubmissionResult = {
    val resultWebPage: Option[WebPage] = evaluateProgram(program, sReporter) match {
      case Some(resultExpr) => convertWebPageExprToClientWebPage(resultExpr, program, sReporter)
      case None => None
    }
    //TODO: give something else than "" (the actual log of the evaluation/conversion process for example)
    SourceCodeSubmissionResult(resultWebPage, "")
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
        sReporter.report(Error, "lookupFunDef(\"" + fullNameOfTheFunctionToEvaluate + "\") gave no result")
        return None
      }
    }
    abstractEvaluator.eval(FunctionInvocation(mainFunDef.typed, List())) match {
      case EvaluationResults.Successful((resultExpr, evaluationTreeExpr)) => {
        sReporter.report(Info, "Ended evaluation of the submitted program.")
        //TODO: should also export the evaluationTreeExpr
        sReporter.report(Info, "resultExpr: "+ resultExpr)
        Some(resultExpr)
      }
      case EvaluationResults.EvaluatorError(msg) => {
        sReporter.report(Error, "EvaluationError during evaluation of the program by leon: "+msg)
        None
      }
      case EvaluationResults.RuntimeError(msg) => {
        sReporter.report(Error, "RuntimeError during evaluation of the program by leon: "+msg)
        None
      }
    }
  }

  private def convertWebPageExprToClientWebPage(webPageExpr: Expr, program: Program, sReporter: ServerReporter): Option[WebPage] = {


    case class ExceptionDuringConversion(msg:String) extends Exception

    sReporter.report(Info, "Starting conversion of the leon Expr returned by the submitted program to a WebPage...")
    sReporter.report(Info, "webPageExpr: "+ webPageExpr)

    val result = try {
      /** Looking up the case classes of webDSL_Leon**/
      def lookupCaseClass(program: Program)(caseClassFullName: String): CaseClassDef = {
        program.lookupCaseClass(caseClassFullName) match {
          case Some(classDef) => classDef
          case None => {
            val msg = "lookupCaseClass(\"" + caseClassFullName + "\") gave no result"
            sReporter.report(Error, msg)
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

      val s =
        """
          |
        """.stripMargin

      def unExpr(e: Expr): Any = {
        sReporter.report(Info, "unExpr was called")
        e match {
          case CaseClass(CaseClassType(caseClassDef, targs), args) => {
            constructorMap.get(caseClassDef) match {
              case Some(constructor) => constructor(args.map (unExpr): _*)
              case None =>
                val msg = s"""
                             |Looked for ${caseClassDef.toString} in the constructorMap, but did not find anything. Throwing exception.
                             |   Maybe ${caseClassDef.toString} is not registered in server/app/programEvaluator/WebDescriptionClassesRegister.
                  """.stripMargin
                sReporter.report(Error, msg)
                throw ExceptionDuringConversion(msg)
            }
//            callConstructorWithNArgs(constructor, argNb, args.map(unExpr).toList)
          }
          case l:Literal[_] => l.value
          case _ => {sReporter.report(Info, "Default case, the expr was: "+ e)}
        }
      }

//      def callConstructorWithNArgs(constructor: universe.MethodMirror, argNumber: Int, args: List[Any]) : Any = {
//        constructor(args:_*)
//      }
      Some(unExpr(webPageExpr).asInstanceOf[WebPage])

//      def unExprList(listExpr: Expr): leon.collection.List[AnyRef] = {
//
//      }

      /*webPageExpr match {
        case CaseClass(CaseClassType(`webPageClassDef`, List()), l) =>
          WebPage(evalList(l.head), eval)
      }

          def unExpr(expr: Expr):  = {
          }
        }*/
//
//      //TODO: Possible improvement: Making a subclass of List in the webDSL_Client and using it instead of List.
//      //TODO: It will make the switching to leon.collecton.List in the webDSL_Client easier (if it is ever done, that is).
//      def evalList(leonListExpr: Expr): List[WebStuff] = {
//        leonListExpr match {
//          case program.library.Cons => List()
//        }
//      }
//      def evalWebStuffExpr(webStuffExpr: Expr): WebStuff = {
//        ???
////        webStuffExpr match {
////          case
////        }
//      }
    }
    catch {
      case ExceptionDuringConversion(msg) => {
        sReporter.report(Error, msg)
        return None
      }
    }
    sReporter.report(Info, "Ended conversion of the leon Expr returned by the submitted program to a WebPage.")
    result
    //TODO: The next line is a dummy implementation. To be removed later.
//    return Some(WebPage(leon.collection.List(), leon.collection.List()))
  }
}
/*
[quote="PsiComa"][quote="cyrusa"]Is the FAQ from TI3 any relevant to anything in SA?[/quote]
Absolutely, because SA is a bunch of changes to those rules. If they originally couldn't buy tech to get CC, they SURE AS HELL wouldn't be allowed in SA either. Cause I wouldn't ADD a shitty rule like that. It makes more sense not removing a shitty rule than adding one.[/quote]

I meant that we considered this possible problem and came up with a clean-looking solution (Virus cannot use the secondary of Technology).
After that point, I think it is pointless to try to respect what the official FAQ says on the subject if it is different from the solution we came up with.
 */
