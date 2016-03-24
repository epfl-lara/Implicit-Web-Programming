package services

import bootstrapSourceCode.BootstrapSourceCodeGetter
import leon.purescala.Definitions.Program
import memory.Memory
import programEvaluator.{LeonProgramMaker, ProgramEvaluator}
import serverReporter.ServerReporter
import shared.{Api, SourceCodeSubmissionResult}

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
        Memory.sourceCode = sourceCode
        return ProgramEvaluator.evaluateAndConvertResult(program, serverReporter)
      case None =>
        return SourceCodeSubmissionResult(None, "leon did not manage to create a Program out of the source code")
    }
  }
}