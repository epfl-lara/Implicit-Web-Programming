package services

import bootstrapSourceCode.BootstrapSourceCodeGetter
import leon.purescala.Definitions.Program
import memory.Memory
import programEvaluator.{LeonProgramMaker, ProgramEvaluator}
import serverReporter.ServerReporter
import shared.{Api, SourceCodeSubmissionResult, StringModification, StringModificationSubmissionResult}

/**
  * Created by dupriez on 2/12/16.
  */
class ApiService extends Api{
  override def getBootstrapSourceCode(): String = {
    val serverReporter = new ServerReporter
    val bootstrapSourceCode = BootstrapSourceCodeGetter.getBootstrapSourceCode(serverReporter)
    serverReporter.flushMessageQueue(msg => println(msg))
    bootstrapSourceCode
  }

  override def submitSourceCode(sourceCode: String): SourceCodeSubmissionResult = {
    val serverReporter = new ServerReporter
    serverReporter.printReportsInConsole = true
    LeonProgramMaker.makeProgram(sourceCode, serverReporter) match {
      case Some(program) =>
        ProgramEvaluator.evaluateAndConvertResult(program, sourceCode, serverReporter) match {
          case (Some((webPage, sourceMap)), evaluationLog) =>
            Memory.sourceMap = sourceMap
            return SourceCodeSubmissionResult(Some(webPage), evaluationLog)
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
    StringModificationSubmissionResult(Some("hey"), "")
  }
}