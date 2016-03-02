package services

import bootstrapSourceCode.BootstrapSourceCodeGetter
import leon.{LeonContext, LeonFatalError, DefaultReporter, Pipeline}
import leon.frontends.scalac.{ExtractionPhase, ClassgenPhase}
import leon.purescala.Definitions.Program
import leon.utils.{NoPosition, TemporaryInputPhase, PrintTreePhase}
import shared.webpageBuildingDSL.{BlankWebPage, WebPage, ErrorWebPage, WebpageBuildingDSLFilesPathsProvider}
import trash.manipulatedFiles.SourceCodeManager
import serverReporter.{Info, ServerReporter}
import shared.{SourceCodeProcessingResult, Api}

import scala.io.BufferedSource

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

  override def submitSourceCode(sourceCode: String): SourceCodeProcessingResult = {
    val relativePathsToWebpageBuildingDSLFiles = WebpageBuildingDSLFilesPathsProvider.relativePathsToWebpageBuildingDSLFiles

    val serverReporter = new ServerReporter
    //    SourceCodeManager.rewriteSourceCode(sourceCode, serverReporter)

    val leonReporter = new DefaultReporter(Set())
    val ctx = leon.Main.processOptions(Seq()).copy(reporter = leonReporter)
    ctx.interruptManager.registerSignalHandler()
    val pipeline: Pipeline[List[String], Program] =
        ClassgenPhase andThen
        ExtractionPhase /*andThen*/
        //new PreprocessingPhase(xlangF, gencF)
//        PrintTreePhase("Output of leon")
    val pipelineInput = TemporaryInputPhase(ctx, (List(sourceCode), relativePathsToWebpageBuildingDSLFiles))

    case class PipelineRunResult(val msg: String, val programOption: Option[Program])

    def runPipeline(pipeline: Pipeline[List[String], Program], pipelineInput: List[String], leonContext: LeonContext) : PipelineRunResult = {
      try {
        serverReporter.report(Info, "Running pipeline")
        val (context, program) = pipeline.run(leonContext,pipelineInput)
        PipelineRunResult("Succesful run", Some(program))
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
          PipelineRunResult("Failure run, LeonFatalError caught", None)
        case e: Throwable =>
          //          return SuccessCompile("<h2>Error here:</h2>" + e.getClass + "<br>" + e.getMessage + "<br>" + e.getStackTrace.map(_.toString).mkString("<br>"))
          PipelineRunResult("Failure run, Exception other than LeonFatalError caught", None)
      }
    }

    def executeProgramToGetTheGeneratedWebPageAndTheSourceMap(program: Program): WebPage = {
      //TODO: fill this, it should also return a sourceMap(to be defined)
//      new WebPage
      BlankWebPage
    }

    runPipeline(pipeline, pipelineInput, ctx) match {
      case PipelineRunResult(msg, None) => {
        serverReporter.report(Info, msg)
        SourceCodeProcessingResult(success = false, Some("error when running leon pipeline: "+msg), ErrorWebPage)
      }
      case PipelineRunResult(msg, Some(program)) => {
        serverReporter.report(Info, msg)
        //TODO: call executeProgramToGetTheGeneratedWebPageAndTheSourceMap and send the generated webpage and the source map
        SourceCodeProcessingResult(success = true, None, executeProgramToGetTheGeneratedWebPageAndTheSourceMap(program))
      }
    }
  }
}
