package programEvaluator

import leon.DefaultReporter
import leon.evaluators.{EvaluationResults, AbstractEvaluator}
import leon.purescala.Definitions.{CaseClassDef, Program}
import leon.purescala.Expressions.{CaseClass, FunctionInvocation, Expr}
import leon.purescala.Types.CaseClassType
import leon.webDSL.webDescription._
import serverReporter._
import shared.SourceCodeSubmissionResult

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

    try {
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
      val fullClassNamesList = List(
        "leon.webDSL.webDescription.WebPage",
        "leon.webDSL.webDescription.TestWebAttribute1",
        "leon.webDSL.webDescription.TestWebElement1",
//        "leon.webDSL.webDescription.WebElement",
//        "leon.webDSL.webDescription.WebAttribute",
//        "leon.webDSL.webDescription.WebPageAttribute",
        "leon.webDSL.webDescription.TestWebPageAttribute1"
      )

      val caseClassDefList = fullClassNamesList.map(lookupCaseClass(program))

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
    //TODO: The next line is a dummy implementation. To be removed later.
    return Some(WebPage(leon.collection.List(), leon.collection.List()))
  }
}
