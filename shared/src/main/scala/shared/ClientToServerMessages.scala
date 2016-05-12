package shared
import boopickle.Default._
/**
  * Created by dupriez on 2/12/16.
  */

sealed trait ClientToServerMessage{
//  var callback: ServerAnswerType => Unit = ()
//  def getBootstrapSourceCode(): Either[String, ServerError]
//  def submitSourceCode(sourceCode: SourceCodeSubmissionNetwork): SourceCodeSubmissionResultNetwork
//  def submitStringModification(stringModificationForNetwork: StringModificationForNetwork): StringModificationSubmissionResultForNetwork
}

sealed trait FunctionCall[+ServerAnswerType] extends ClientToServerMessage

//case class GetBootstrapSourceCode() extends FunctionCall[Either[String, ServerError]]
case class GetBootstrapSourceCode() extends FunctionCall[Option[String]]
case class SubmitSourceCode(sourceCode: SourceCodeSubmissionNetwork) extends FunctionCall[SourceCodeSubmissionResultNetwork]
case class SubmitStringModification(stringModificationForNetwork: StringModificationForNetwork) extends FunctionCall[StringModificationSubmissionResultForNetwork]
//Without this useless type parameter in ClientToServerMessages_withID, the code:
//"val clientToServerMessage_withID = ClientToServerMessages_withID(id, clientToServerMessage)" where clientToServerMessage was a ClientToServerMessage[ServerAnswerType]
//was not compiling because "ClientToServerMessages_withID expects its second argument to be of type ClientToServerMessage, while it is of type ClientToServerMessage[ServerAnswerType]
//case class ClientToServerMessages_withID(id: Long, clientToServerMessage_forTransmission: ClientToServerMessages_forTransmission)
case class ClientToServerMessages_withID(id: Long, clientToServerMessage: ClientToServerMessage)

abstract class CallbackForClientToServerMessage_Abstract {
  def applyCallback(serverAnswer: Any): Unit
}
case class CallbackForClientToServerMessage[ServerAnswerType](callback: ServerAnswerType => Unit) extends CallbackForClientToServerMessage_Abstract {
  override def applyCallback(serverAnswer: Any): Unit = {
    callback(serverAnswer.asInstanceOf[ServerAnswerType])
  }
}

//case class ClientToServerMessages_withCallback[ServerAnswerType](callback: ServerAnswerType => Unit, clientToServerMessage: ClientToServerMessage[ServerAnswerType]) {
//  def applyCallback(serverAnswer: Any) = {
//    callback(serverAnswer.asInstanceOf[ServerAnswerType])
//  }
//}

//case class AClientToServerMessages_withID(id: Long, aClientToServerMessage: AClientToServerMessage)
//sealed trait AClientToServerMessage
////Messages from the Client to the Server that do not ask for answers will only extends ClientToServerMessage
//sealed trait AServerToClientMessage
////Messages from the Server to the Client that are not answer to some clientToServerMessage will only extends ServerToClientMessage
//sealed trait FunctionCall[ReturnType] extends AClientToServerMessage with AServerToClientMessage
//case class AGetBootstrapSourceCode() extends FunctionCall[Either[String, ServerError]]
//case class ASubmitSourceCode(sourceCode: SourceCodeSubmissionNetwork) extends FunctionCall[SourceCodeSubmissionResultNetwork]
//case class ASubmitStringModification(stringModificationForNetwork: StringModificationForNetwork) extends FunctionCall[StringModificationSubmissionResultForNetwork]
