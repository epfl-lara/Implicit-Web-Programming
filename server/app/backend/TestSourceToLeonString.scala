package backend

import leon.frontends.scalac.{ExtractionPhase, ClassgenPhase}
import leon.purescala.Definitions.Program
import leon.utils.{OffsetPosition, RangePosition, Position, PrintTreePhase}
import leon.{LeonFatalError, Pipeline, DefaultReporter}

/**
  * Created by dupriez on 2/10/16.
  */
object TestSourceToLeonString {

  def path2LeonString(pathToSourceProgram: String) : String = {
    val mainReporter = new DefaultReporter(Set())
    val pipeline: Pipeline[List[String], Program] =
      ClassgenPhase andThen
        ExtractionPhase andThen
        //new PreprocessingPhase(xlangF, gencF)
        PrintTreePhase("Output of leon")

    class CaptureAndCollectReporter extends DefaultReporter(Set()) {
      var leonOutput = ""

      def addToLeonOutput(s: String) = {
        leonOutput = leonOutput + s
      }

      override def emit(msg: Message): Unit = {
        //        println(reline(severityToPrefix(msg.severity), smartPos(msg.position) + msg.msg.toString))
        addToLeonOutput(reline(severityToPrefix(msg.severity), smartPos(msg.position) + msg.msg.toString))
        printLineContent(msg.position)
      }

      override def printLineContent(pos: Position): Unit = {
        def printLineContent(pos: Position): Unit = {
          getLine(pos) match {
            case Some(line) =>
              //              println(blankPrefix+line)
              addToLeonOutput(blankPrefix + line)
              pos match {
                case rp: RangePosition =>
                  val bp = rp.focusBegin
                  val ep = rp.focusEnd

                  val carret = if (bp.line == ep.line) {
                    val width = Math.max(ep.col - bp.col, 1)
                    "^" * width
                  } else {
                    val width = Math.max(line.length + 1 - bp.col, 1)
                    ("^" * width) + "..."
                  }

                  //                  println(blankPrefix+(" " * (bp.col - 1) + Console.RED+carret+Console.RESET))
                  addToLeonOutput(blankPrefix + (" " * (bp.col - 1) + Console.RED + carret + Console.RESET))

                case op: OffsetPosition =>
                  //                  println(blankPrefix+(" " * (op.col - 1) + Console.RED+"^"+Console.RESET))
                  addToLeonOutput(blankPrefix + (" " * (op.col - 1) + Console.RED + "^" + Console.RESET))
              }
            case None =>
          }
        }
      }
    }
    val captureAndCollectReporter = new CaptureAndCollectReporter

    val ctx = leon.Main.processOptions(Seq()).copy(reporter = /*leonReporter*/ captureAndCollectReporter)
    ctx.interruptManager.registerSignalHandler()

    sealed abstract class PipelineRunResult(msg: String) {
      def printMsg() = {
        mainReporter.info(msg)
      }
    }
    case class PipelineRunSuccess(msg: String) extends PipelineRunResult(msg)
    case class PipelineRunFailure(msg: String) extends PipelineRunResult(msg)


    def runPipeline(pipeline: Pipeline[List[String], Program], input: List[String]): PipelineRunResult = {
      try {
        mainReporter.info("Running pipeline")
        pipeline.run(ctx, input)
        PipelineRunSuccess("Succesful run")
      } catch {
        case e: LeonFatalError =>
          PipelineRunFailure("Failure run, LeonFatalError caught")
        case e: Throwable =>
          PipelineRunFailure("Failure run, Exception other than LeonFatalError caught")
      }
    }

    println("LeonInput = ")
    runPipeline(pipeline, List(pathToSourceProgram)).printMsg()

    return captureAndCollectReporter.leonOutput
  }
}