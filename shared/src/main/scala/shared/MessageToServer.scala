package shared
/**
  * Created by dupriez on 2/12/16.
  */

sealed trait MessageToServer

sealed trait MessageToServerExpecting[ServerAnswerType <: MessageFromServer] extends MessageToServer

case class GetBootstrapSourceCode() extends MessageToServerExpecting[GetBootstrapSourceCode_answer]
case class SubmitSourceCode(source: String, requestId: Int) extends MessageToServerExpecting[SubmitSourceCodeResult]
case class SubmitStringModification(stringModification: StringModification, sourceCodeId: Int, stringModID: Int) extends MessageToServerExpecting[SubmitStringModificationResult]

object MessageToServer {
  import boopickle.Default._

  implicit val msgToServerPickler = generatePickler[MessageToServer]
}