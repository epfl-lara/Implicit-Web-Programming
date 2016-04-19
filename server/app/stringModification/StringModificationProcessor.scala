package stringModification

import leon.purescala.Expressions.{CaseClass, Expr}
import leon.purescala.Types.CaseClassType
import leon.webDSL.webDescription.Text
import logging.OptionValWithLog
import logging.serverReporter.{Debug, Info, ServerReporter}
import memory.Memory
import shared.{StringModification, StringModificationSubmissionResult}

/**
  * Created by dupriez on 4/18/16.
  */
object StringModificationProcessor {
  private def failure(functionName: String, failureCauseLog: String) =
    StringModificationSubmissionResult(None, s"Failure in StringModificationProcessor: function ${functionName} failed because: ${failureCauseLog}")

  def process(strMod: StringModification, serverReporter: ServerReporter) : StringModificationSubmissionResult = {
    val sReporter = serverReporter.startProcess("String Modification Processor")
    val (weID, modWebAttr, newVal) = strMod match {case StringModification(w, m, n)=> (w,m,n)}
    sReporter.report(Info,
      s"""String modification currently processed:
        | webElementID: $weID
        | Modified WebAttribute: $modWebAttr
        | New value: $newVal
      """.stripMargin)

    val unevalExprOfWebElem = Memory.sourceMap.webElementIDToExpr(weID) match {
      case OptionValWithLog(Some(value), log) =>
        sReporter.report(Info, s"Obtained the Expr of the unevaluated webElement from SouceMap: $value")
        value
      case OptionValWithLog(None, log) =>
        sReporter.report(Info, "sourceMap query for the Expr of the unevaluated webElement")
        return failure("StringModificationProcessor:process", log)
    }

    val sourceCode = Memory.sourceMap.sourceCode

    modWebAttr match {
      case Text => textAttributeModification(unevalExprOfWebElem)
    }

    def textAttributeModification(unevalExprOfWebElem: Expr) = {
      sReporter.report(Debug, "Original sourceCode: "+ sourceCode)
//      unevalExprOfWebElem match {
//        case CaseClass(CaseClassType(`nilCaseClassDef`, targs), args) => leon.collection.List()
//      }

    }

    StringModificationSubmissionResult(Some("heyk"), "log")
  }
}
