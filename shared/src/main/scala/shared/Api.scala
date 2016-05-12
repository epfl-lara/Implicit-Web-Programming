package shared

/**
  * Created by dupriez on 2/12/16.
  */
trait Api {
//  def getBootstrapSourceCode(): Either[String, ServerError]
//  def submitSourceCode(sourceCode: SourceCodeSubmissionNetwork): SourceCodeSubmissionResultNetwork
//  def submitStringModification(stringModificationForNetwork: StringModificationForNetwork): StringModificationSubmissionResultForNetwork

  def sendToServer(message: ClientToServerMessages_withID): ServerToClientMessage_withID
}
