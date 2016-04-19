package services

import bootstrapSourceCode.BootstrapSourceCodeGetter
import leon.purescala.Definitions.Program
import memory.Memory
import programEvaluator.{LeonProgramMaker, ProgramEvaluator}
import logging.serverReporter.{Info, ServerReporter}
import shared._
import stringModification.StringModificationProcessor

/**
  * Created by dupriez on 2/12/16.
  */
class ApiService extends Api{
  override def getBootstrapSourceCode(): Either[String, ServerError] = {
    val serverReporter = new ServerReporter
    val bootstrapSourceCode = BootstrapSourceCodeGetter.getBootstrapSourceCode(serverReporter)
//    serverReporter.flushMessageQueue(msg => println(msg))
    bootstrapSourceCode match {
      case Some(sourceCode) => Left(sourceCode)
      case None => Right(UnableToFetchBootstrapSourceCode())
    }
  }

  override def submitSourceCode(sourceCode: String): SourceCodeSubmissionResult = {
    val serverReporter = new ServerReporter
    LeonProgramMaker.makeProgram(sourceCode, serverReporter) match {
      case Some(program) =>
        ProgramEvaluator.evaluateAndConvertResult(program, sourceCode, serverReporter) match {
          case (Some((webPageWithIDedWebElement, sourceMap)), evaluationLog) =>
            Memory.sourceMap = sourceMap
            return SourceCodeSubmissionResult(Some(webPageWithIDedWebElement), evaluationLog)
          case (None, evaluationLog) =>
            Memory.sourceMap = null
            return SourceCodeSubmissionResult(None,
              s"""
                |ProgramEvaluator did not manage to evaluate and unexpr the result of the leon program.
                | Here is the evaluation log: $evaluationLog
              """.stripMargin)
        }
      case None =>
        return SourceCodeSubmissionResult(None, "leon did not manage to create a Program out of the source code")
    }
  }

  //  def submitHtml(don't know, something that indicate a change made to the html): String //New Source Code


  override def submitStringModification(stringModification: StringModification): StringModificationSubmissionResult = {
    val sReporter = new ServerReporter
    sReporter.report(Info,
      s"""Received a string modification from the client:
         |  webElementID: ${stringModification.webElementID}
         |  modified WebAttribute${stringModification.modifiedWebAttribute}
         |  new value: ${stringModification.newValue}
       """.stripMargin
    )
    val weID = stringModification.webElementID
    val weExprFromSourceMap = Memory.sourceMap.webElementIDToExpr(weID)
    sReporter.report(Info,
      s"""Here's what has been found in the sourceMap for the webElementID $weID:
         |${weExprFromSourceMap}
       """.stripMargin)

    StringModificationProcessor.process(stringModification, sReporter)
//    StringModificationSubmissionResult(Some("hey"), "")
  }
}