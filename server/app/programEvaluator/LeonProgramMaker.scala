package programEvaluator

import leon.frontends.scalac.{ExtractionPhase, ClassgenPhase}
import leon.utils.TemporaryInputPhase
import leon.{LeonFatalError, LeonContext, Pipeline, DefaultReporter}
import leon.purescala.Definitions.{CaseClassDef, Program}
import logging.serverReporter.{ServerReporter, Info, Error}
import shared.SourceCodeSubmissionResult

/**
  * Created by dupriez on 3/22/16.
  */
object LeonProgramMaker {
  def makeProgram(sourceCode: String, sReporter: ServerReporter) : Option[Program] = {
    sReporter.report(Info, "Turning the sourceCode into a leon Program...")
    val leonReporter = new DefaultReporter(Set())
    val ctx = leon.Main.processOptions(Seq()).copy(reporter = leonReporter)
    ctx.interruptManager.registerSignalHandler()
    val pipeline: Pipeline[List[String], Program] =
      ClassgenPhase andThen
        ExtractionPhase /*andThen*/
    //new PreprocessingPhase(xlangF, gencF)
    //        PrintTreePhase("Output of leon")

    //    Commented to test the webpageDSLBis
    //    relativePathsToWebpageBuildingDSLFiles.foreach(pathToFile => logging.serverReporter.report(Info, "Additional file provided to leon: " + pathToFile))

    //Add a line importing the shared.webpageBuildingDSL package to the source code string
    //    Partially Commented to test the webpageDSLBis
    val sourceCodeWithImport = /*WebpageBuildingDSLFilesPathsProvider.importLine + sys.props("line.separator") +*/ sourceCode

    val pipelineInput = TemporaryInputPhase(ctx, (List(sourceCodeWithImport), List()))

    case class PipelineRunResult(val msg: String, val programOption: Option[Program])

    def runPipeline(pipeline: Pipeline[List[String], Program], pipelineInput: List[String], leonContext: LeonContext) : Option[Program] = {
      try {
        sReporter.report(Info, "Running leon pipeline", 1)
        val (context, program) = pipeline.run(leonContext,pipelineInput)
        sReporter.report(Info, "Leon pipeline run successful", 1)
        Some(program)
      } catch {
        case e: LeonFatalError =>
          //          ctx.reporter.errorMessage match {
          //            case Some(msg) => return FailureCompile(if (msg.position != NoPosition) { msg.position+": " } else { "" },
          //              msg.severity.toString,
          //              msg.msg.toString
          //            )
          //            case None =>
          //          }
          //          return SuccessCompile("<h2>Error here:</h2>" + e.getClass + "<br>" + e.getMessage + "<br>" + e.getStackTrace.map(_.toString).mkString("<br>"))
          sReporter.report(Error, "Leon pipeline run failed, LeonFatalError caught", 1)
          None
        case e: Throwable =>
          //          return SuccessCompile("<h2>Error here:</h2>" + e.getClass + "<br>" + e.getMessage + "<br>" + e.getStackTrace.map(_.toString).mkString("<br>"))
          sReporter.report(Error, "Leon pipeline run failed, exception other than LeonFatalError caught", 1)
          None
      }
    }
    val result = runPipeline(pipeline, pipelineInput, ctx)
    result match {
      case Some(program) =>
        sReporter.report(Info, "Program: "+program, 1)
        result
      case _ =>
        result
    }
  }
}
