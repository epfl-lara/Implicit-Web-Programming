package shared

/**
  * Created by dupriez on 2/12/16.
  */
sealed trait MessageFromServer

case class GetBootstrapSourceCode_answer(bootstrapSourceCode: Option[String]) extends MessageFromServer
case class SubmitSourceCodeResult(result: SourceCodeSubmissionResult, requestId: Int) extends MessageFromServer
case class SubmitStringModification_answer(
       stringModificationSubmissionResult: StringModificationSubmissionResult,
       requestSourceId: Int,
       requestStringModSubResID: Int) extends MessageFromServer
//                                          d: Either[Int,Int],
//                                          a: DisambiguationProblemWithQuestion[T forSome{type T}],

object MessageFromServer {
  import boopickle.Default._
  import Picklers._
  implicit val pickler = generatePickler[MessageFromServer]
}