package stringModification

import logging.OptionValWithLog
import logging.serverReporter.{Info, ServerReporter}
import memory.Memory
import shared.{StringModification, StringModificationSubmissionResult}

/**
  * Created by dupriez on 4/18/16.
  */
object StringModificationProcessor {
  private def failure(functionName: String, failureCauseLog: String) =
    StringModificationSubmissionResult(None, s"Failure in StringModificationProcessor: function ${functionName} failed because: ${failureCauseLog}")

  def process(strMod: StringModification, sReporter: ServerReporter) : StringModificationSubmissionResult = {
    sReporter.report(Info, "StringModificationProcessor starts processing a stringModificationSubmission")
    val (weID, modWebAttr, newVal) = strMod match {case StringModification(w, m, n)=> (w,m,n)}

    val unevalExprOfWebElem = Memory.sourceMap.webElementIDToExpr(weID) match {
      case OptionValWithLog(Some(value), log) => value
      case OptionValWithLog(None, log) => return return failure("process", log)
    }

    StringModificationSubmissionResult(Some("heyk"), "log")
  }
}
