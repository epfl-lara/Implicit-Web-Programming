package shared

/**
  * Created by dupriez on 2/12/16.
  */
sealed trait ServerToClientMessage {
//  def getBootstrapSourceCode(): Either[String, ServerError]
//  def submitSourceCode(sourceCode: SourceCodeSubmissionNetwork): SourceCodeSubmissionResultNetwork
//  def submitStringModification(stringModificationForNetwork: StringModificationForNetwork): StringModificationSubmissionResultForNetwork
}
//case class GetBootstrapSourceCode_answer(bootstrapSourceCode: Either[String, ServerError]) extends ServerToClientMessage
//case class SubmitSourceCode_answer(sourceCodeSubmissionResultForNetwork: SourceCodeSubmissionResultNetwork) extends ServerToClientMessage
//case class SubmitStringModification_answer(stringModificationSubmissionResultForNetwork: StringModificationSubmissionResultForNetwork) extends ServerToClientMessage

//case class ServerAnswer[AnswerType: ClientToServerMessage](id: Long, answer: AnswerType) extends ServerToClientMessage

//case class ServerAnswer[T](id: Long, message: ClientToServerMessage[T]) extends ServerToClientMessage

//case class ServerAnswer[ServerAnswerType, ClientMessageType<:ClientToServerMessage[ServerAnswerType]](id: Long, answer: ServerAnswerType) extends ServerToClientMessage


sealed trait FunctionReturn extends ServerToClientMessage{
  def returnValue: Any
}

//case class GetBootstrapSourceCode_answer(bootstrapSourceCode: Either[String, ServerError]) extends FunctionReturn
case class GetBootstrapSourceCode_answer(bootstrapSourceCode: Option[String]) extends FunctionReturn{
  override def returnValue = bootstrapSourceCode
}
case class SubmitSourceCode_answer(sourceCodeSubmissionResult: SourceCodeSubmissionResultNetwork) extends FunctionReturn{
  override def returnValue = sourceCodeSubmissionResult
}
case class SubmitStringModification_answer(stringModificationSubmissionResult: StringModificationSubmissionResultForNetwork) extends FunctionReturn{
  override def returnValue = stringModificationSubmissionResult
}

case class ServerToClientMessage_withID(id: Long, serverToClientMessage: ServerToClientMessage)
