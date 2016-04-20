package stringModification

import leon.LeonContext
import leon.purescala.Common
import leon.purescala.Common._
import leon.purescala.Definitions.CaseClassDef
import leon.purescala.Expressions.{CaseClass, Expr, StringConcat, StringLiteral}
import leon.purescala.Types.CaseClassType
import leon.solvers.string.StringSolver
import leon.solvers.string.StringSolver.{Assignment, Equation, Problem, StringForm}
import leon.synthesis.FileInterface
import leon.webDSL.webDescription.{Header, Paragraph, Text, WebElement}
import logging.OptionValWithLog
import logging.serverReporter.{Debug, Info, ServerReporter}
import memory.Memory
import programEvaluator.SourceMap
import services.ApiService
import shared.{SourceCodeSubmissionResult, StringModification, StringModificationSubmissionResult}

/**
  * Created by dupriez on 4/18/16.
  */
object StringModificationProcessor {

   case class StringModificationProcessingException(msg:String) extends Exception
   def failure(failureMessage: String) = {
     throw StringModificationProcessingException(s"Failure in StringModificationProcessor: ${failureMessage}")
   }

  private def getWebElemAndUnevalExprOfWebElemFromSourceMap(weID: Int, sourceMap: SourceMap, sReporter: ServerReporter) = {

    val webElem = sourceMap.webElementIDToWebElement(weID) match {
      case OptionValWithLog(Some(value), log) =>
        sReporter.report(Info, s"Obtained the webElement (id=$weID) from SourceMap: $value")
        value
      case OptionValWithLog(None, log) =>
        sReporter.report(Info, s"SourceMap query for the webElement (id=$weID) failed")
        failure("SourceMap query for the Expr of the unevaluated webElement failed")
    }

    val unevalExprOfWebElem = sourceMap.webElementIDToExpr(weID) match {
      case OptionValWithLog(Some(value), log) =>
        sReporter.report(Info, s"Obtained the Expr of the unevaluated webElement (id=$weID) from SourceMap: $value")
        value
      case OptionValWithLog(None, log) =>
        sReporter.report(Info, "SourceMap query for the Expr of the unevaluated webElement failed")
        failure("SourceMap query for the Expr of the unevaluated webElement failed")
    }

    (webElem, unevalExprOfWebElem)
  }

  private def getCaseClassDefValOrFail(optionValWithLog: OptionValWithLog[CaseClassDef]) : CaseClassDef = {
    optionValWithLog match{
      case OptionValWithLog(Some(value), log) => value
      case OptionValWithLog(None, log) => failure(log)
    }
  }

  def process(strMod: StringModification, serverReporter: ServerReporter) : StringModificationSubmissionResult = {
    val sReporter = serverReporter.startProcess("String Modification Processor")


    val (weID, modWebAttr, newVal) = strMod match {case StringModification(w, m, n)=> (w,m,n)}
    sReporter.report(Info,
      s"""String modification currently processed:
        | webElementID: $weID
        | Modified WebAttribute: $modWebAttr
        | New value: $newVal
      """.stripMargin)

    val result = try{

    }
    catch {
      case StringModificationProcessingException(msg) =>
        StringModificationSubmissionResult(None, msg)
    }

    val sourceMap = Memory.sourceMap
    val (webElement, unevalExprOfWebElem) = getWebElemAndUnevalExprOfWebElemFromSourceMap(weID, sourceMap, sReporter)
    val sourceCode = sourceMap.sourceCode

    val webPageCaseClassDef = getCaseClassDefValOrFail(sourceMap.webPage_webElementCaseClassDef(sReporter))
    val paragraphCaseClassDef = getCaseClassDefValOrFail(sourceMap.paragraph_webElementCaseClassDef(sReporter))
    val headerCaseClassDef = getCaseClassDefValOrFail(sourceMap.header_webElementCaseClassDef(sReporter))
    val divCaseClassDef = getCaseClassDefValOrFail(sourceMap.div_webElementCaseClassDef(sReporter))
    val consCaseClassDef = getCaseClassDefValOrFail(sourceMap.leonCons_caseClassDef(sReporter))
    val nilCaseClassDef = getCaseClassDefValOrFail(sourceMap.leonNil_caseClassDef(sReporter))

    val (argumentIndexOfModifiedStringWebAttrInWebElem, originalText) = modWebAttr match {
      case Text => webElement match {
        case Paragraph(text) => (0,text)
        case Header(text,_) => (0,text)
        case _ => failure(s"WebElement $webElement has no Text webAttr to modify according to the pattern matching in StringModificationProcessor")
      }
    }
    val textExpr = unevalExprOfWebElem match {
      case CaseClass(CaseClassType(_, _), argSeq) => argSeq(argumentIndexOfModifiedStringWebAttrInWebElem)
    }

    def textExprToStringForm(textExpr: Expr, identifierToTextExprMap: scala.collection.mutable.Map[Identifier, Expr]): StringForm = {
      textExpr match {
        case StringLiteral(string) =>
          val identifier = Common.FreshIdentifier("l:"+textExpr.getPos.line+",c:"+textExpr.getPos.col).copiedFrom(textExpr)
          identifierToTextExprMap(identifier) = textExpr
          List(Right(identifier))
        case StringConcat(tExpr1, tExpr2) => textExprToStringForm(tExpr1,identifierToTextExprMap) ++ textExprToStringForm(tExpr2,identifierToTextExprMap)
      }
    }

    sReporter.report(Info, "Original text= " + originalText)
    val identifierToTextExprMap: scala.collection.mutable.Map[Identifier, Expr] = scala.collection.mutable.Map()
    val stringForm = textExprToStringForm(textExpr, identifierToTextExprMap)
    sReporter.report(Info, "StringForm= " + stringForm)
    val problem: Problem = List((stringForm, newVal))
    sReporter.report(Info, "StringSolver problem: "+StringSolver.renderProblem(problem))
    val solutionStream: Stream[Assignment] = StringSolver.solve(problem)
    sReporter.report(Info, "First 5 StringSolver solutions: "+solutionStream.take(5).foldLeft("")((str, assignment)=>str+assignment.toString()))
    val firstSol = solutionStream.headOption match{
      case Some(value) => value
      case None => failure("StringSolver returned no solutions")
    }
    implicit val context = LeonContext.empty
    val fileInterface = new FileInterface(context.reporter)
    val newSourceCode = firstSol.toList
//      Apply the modifications from the bottom to the top, to keep the line numbers consistent for the next modifications.
      .sortBy({case (identifier, str) => identifier.getPos})
      .reverse
      .foldLeft(sourceCode)(
        {case (sCode, (identifier, string))=> fileInterface.substitute(sCode, identifier, StringLiteral(string))}
      )
    sReporter.report(Info, "New source code: "+ newSourceCode)
    val apiService = new ApiService
    sReporter.report(Info, "Submitting the new source code (as if the client did it)")
    apiService.submitSourceCode(newSourceCode) match {
      case SourceCodeSubmissionResult(Some(webPageIDed), _) =>
        sReporter.report(Info, "Sending back to client the new source code and a WebPage with IDed WebElements")
        StringModificationSubmissionResult(Some((newSourceCode, webPageIDed)), "")
      case SourceCodeSubmissionResult(None, log) =>
        sReporter.report(Info, "The submission of the new source code failed because: "+log)
        StringModificationSubmissionResult(None, log)
    }

//    def stringWebAttrModification(argumentIndexOfModifiedStringWebAttrInWebElem: Int, webElement: WebElement, unevalExprOfWebElem: Expr) = {
//      sReporter.report(Debug, "Original sourceCode: "+ sourceCode)
//      unevalExprOfWebElem match {
//        case CaseClass(CaseClassType(`paragraphCaseClassDef`, targs), Seq(textExpr)) => {
//          //          Modifying the Text of a Paragraph
//          def textExprToStringForm(textExpr: Expr, identifierToTextExprMap: scala.collection.mutable.Map[Identifier, Expr]): StringForm = {
//            textExpr match {
//              case StringLiteral(string) =>
//                val identifier = Common.FreshIdentifier("l:"+textExpr.getPos.line+",c:"+textExpr.getPos.col).copiedFrom(textExpr)
//                identifierToTextExprMap(identifier) = textExpr
//                List(Right(identifier))
//              case StringConcat(tExpr1, tExpr2) => textExprToStringForm(tExpr1,identifierToTextExprMap) ++ textExprToStringForm(tExpr2,identifierToTextExprMap)
//            }
//          }
//          val originalText = webElement match {
//            case Paragraph(text) => text
//            // This second case should not happen since we normally already check that the type of the WebElement and
//            //  the type of the WebElement the expr represents are the same when filling the sourceMap.
//            case _ => failure("sourceMap gave the unevaluated Expr of a Paragraph, but the WebElement registered with the same key is not a Paragraph")
//          }
//          sReporter.report(Info, "Original text= " + originalText)
//          val identifierToTextExprMap: scala.collection.mutable.Map[Identifier, Expr] = scala.collection.mutable.Map()
//          val stringForm = textExprToStringForm(textExpr, identifierToTextExprMap)
//          sReporter.report(Info, "StringForm= " + stringForm)
//          val problem: Problem = List((stringForm, newVal))
//          sReporter.report(Info, "StringSolver problem: "+StringSolver.renderProblem(problem))
//          val solutionStream: Stream[Assignment] = StringSolver.solve(problem)
//          sReporter.report(Info, "First 5 StringSolver solutions: "+solutionStream.take(5).foldLeft("")((str, assignment)=>str+assignment.toString()))
//          val firstSol = solutionStream.headOption match{
//            case Some(value) => value
//            case None => failure("StringSolver returned no solutions")
//          }
//          implicit val context = LeonContext.empty
//          val fileInterface = new FileInterface(context.reporter)
//          val newSourceCode = firstSol.toList
//            .sortBy({case (identifier, str) => identifier.getPos})
//            .reverse
//            .foldLeft(sourceCode)(
//              {case (sCode, (identifier, string))=> fileInterface.substitute(sCode, identifier, StringLiteral(string))}
//            )
//          sReporter.report(Info, "New source code: "+ newSourceCode)
//        }
//        case CaseClass(CaseClassType(`headerCaseClassDef`, targs), _) => {
////          Not implemented yet
//          StringModificationSubmissionResult(Some("heyk"), "log")
//        }
//        case _ =>
////        Should not happen?
//          StringModificationSubmissionResult(Some("heyk"), "log")
//      }
//    }

//    stringWebAttrModification(argumentIndexOfModifiedStringWebAttrInWebElem, webElement, unevalExprOfWebElem)

//    StringModificationSubmissionResult(Some("heyk"), "log")
  }
}
