package stringModification

import leon.purescala.Common._
import leon.purescala.Definitions.CaseClassDef
import leon.purescala.Expressions.{CaseClass, Expr, StringConcat, StringLiteral}
import leon.purescala.Types.CaseClassType
import leon.solvers.string.StringSolver.{Equation, Problem, StringForm}
import leon.webDSL.webDescription.{Paragraph, Text}
import logging.OptionValWithLog
import logging.serverReporter.{Debug, Info, ServerReporter}
import memory.Memory
import shared.{StringModification, StringModificationSubmissionResult}

/**
  * Created by dupriez on 4/18/16.
  */
object StringModificationProcessor {

  def process(strMod: StringModification, serverReporter: ServerReporter) : StringModificationSubmissionResult = {
    val sReporter = serverReporter.startProcess("String Modification Processor")

    case class StringModificationProcessingException(msg:String) extends Exception
    def failure(failureMessage: String) = {
      throw StringModificationProcessingException(s"Failure in StringModificationProcessor: ${failureMessage}")
    }

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

    val unevalExprOfWebElem = sourceMap.webElementIDToExpr(weID) match {
      case OptionValWithLog(Some(value), log) =>
        sReporter.report(Info, s"Obtained the Expr of the unevaluated webElement (id=$weID) from SourceMap: $value")
        value
      case OptionValWithLog(None, log) =>
        sReporter.report(Info, "SourceMap query for the Expr of the unevaluated webElement failed")
        failure("SourceMap query for the Expr of the unevaluated webElement failed")
    }
    val webElement = sourceMap.webElementIDToWebElement(weID) match {
      case OptionValWithLog(Some(value), log) =>
        sReporter.report(Info, s"Obtained the webElement (id=$weID) from SourceMap: $value")
        value
      case OptionValWithLog(None, log) =>
        sReporter.report(Info, s"SourceMap query for the webElement (id=$weID) failed")
        failure("SourceMap query for the Expr of the unevaluated webElement failed")
    }

    val sourceCode = sourceMap.sourceCode

    def getCaseClassDefValOrFail(optionValWithLog: OptionValWithLog[CaseClassDef]) : CaseClassDef = {
      optionValWithLog match{
        case OptionValWithLog(Some(value), log) => value
        case OptionValWithLog(None, log) => failure(log)
      }
    }
    val webPageCaseClassDef = getCaseClassDefValOrFail(sourceMap.webPage_webElementCaseClassDef(sReporter))
    val paragraphCaseClassDef = getCaseClassDefValOrFail(sourceMap.paragraph_webElementCaseClassDef(sReporter))
    val headerCaseClassDef = getCaseClassDefValOrFail(sourceMap.header_webElementCaseClassDef(sReporter))
    val divCaseClassDef = getCaseClassDefValOrFail(sourceMap.div_webElementCaseClassDef(sReporter))
    val consCaseClassDef = getCaseClassDefValOrFail(sourceMap.leonCons_caseClassDef(sReporter))
    val nilCaseClassDef = getCaseClassDefValOrFail(sourceMap.leonNil_caseClassDef(sReporter))

    modWebAttr match {
      case Text => textAttributeModification(unevalExprOfWebElem)
    }

    def textAttributeModification(unevalExprOfWebElem: Expr) = {
      sReporter.report(Debug, "Original sourceCode: "+ sourceCode)
      unevalExprOfWebElem match {
        case CaseClass(CaseClassType(`paragraphCaseClassDef`, targs), Seq(textExpr)) => {
          //          Modifying the Text of a Paragraph
          //To generate a leon Identifier, use the function "freshen" of Common, I guess.
          def textExprToStringForm(textExpr: Expr): StringForm = {
            textExpr match {
              case StringLiteral(string) => List(Left(string))
              case StringConcat(tExpr1, tExpr2) => textExprToStringForm(tExpr1) ++ textExprToStringForm(tExpr2)
            }
          }
          val originalText = webElement match {
            case Paragraph(text) => text
            //              This seconda case should not happen since we normally already check that when filling the sourceMap.
            case _ => failure("sourceMap gave the unevaluated Expr of a Paragraph, but the WebElement registered with the same key is not a Paragraph")
          }
          sReporter.report(Info, "Original text= " + originalText)
          val stringForm = textExprToStringForm(textExpr)
          sReporter.report(Info, "StringForm= " + stringForm)
          val problem: Problem = List((stringForm, originalText))
        }
        case CaseClass(CaseClassType(`headerCaseClassDef`, targs), _) => {
//          Not implemented yet
          StringModificationSubmissionResult(Some("heyk"), "log")
        }
        case _ =>
//        Should not happen?
          StringModificationSubmissionResult(Some("heyk"), "log")
      }
    }
    StringModificationSubmissionResult(Some("heyk"), "log")
  }
}
