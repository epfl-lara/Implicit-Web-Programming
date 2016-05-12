import shared.{SourceCodeSubmissionNetwork, SubmitSourceCode, _}
import scala.concurrent.ExecutionContext.Implicits.global
import autowire._
import boopickle.Default._
import scala.util.{Failure, Success}

/**
  * Created by dupriez on 5/11/16.
  */
//object CallbackForServerMessages {
//  def callbackForServerMessages(serverMessage: ServerToClientMessage): Unit = {
//    serverMessage match {
//      case GetBootstrapSourceCode_answer(bootStrapSourceCode) =>
//        ScalaJS_Main.AceEditor.getBootstrapSourceCode_serverAnswerHandler(bootStrapSourceCode)
//      case s@SubmitSourceCode_answer(sourceCodeSubmissionResultForNetwork) =>
//        ScalaJS_Main.submitSourceCode_serverAnswerHandler(sourceCodeSubmissionResultForNetwork)
//        val requestID : Int = sourceCodeSubmissionResultForNetwork.requestId
//        val callback = submitSourceCode_answer_map(requestID)
//        submitSourceCode_answer_map -= requestID
//        callback(s)
//      case SubmitStringModification_answer(stringModificationSubmissionResultForNetwork) =>
//        ScalaJS_Main.submitStringModification_serverAnswerHandler(stringModificationSubmissionResultForNetwork)
//    }
//  }
//  trait HasMapping[InputType<:ClientToServerMessage[OutputType], OutputType] {
//    private val callbackMapping = scala.collection.mutable.Map[Long, (OutputType => Unit)]()
//    def addMapping(id: Long, callback: OutputType=>Unit) = {
//      callbackMapping += (id -> callback)
//    }
//    def getMapping(id: Long) = {
//      callbackMapping.get(id).get
//    }
//  }
//  object SubmitSourceCodeMapping extends HasMapping[SubmitSourceCode, Either[String, ServerError]]
//
//  implicit var submitSourceCode_answer_map = Map[Int, SubmitSourceCode_answer=>Unit]()
//
//  def apply(submitSourceCode: SubmitSourceCode)(callback: SubmitSourceCode_answer=>Unit) = {
//    val requestID = submitSourceCode.sourceCode.requestId
//    submitSourceCode_answer_map += requestID -> callback
//    AjaxClient[Api].sendToServer(submitSourceCode).call().onComplete {
//      case Failure(exception) => {
//        println("error during submission of the source code: " + exception)
//        submitSourceCode_answer_map -= requestID
//      }
//      case Success(serverAnswer) => CallbackForServerMessages.callbackForServerMessages(serverAnswer)
//    }
//  }
//
//  object IDGenerator {
//    private var id : Long = 0
//    def genID() = {
//      id+=1
//      id
//    }
//  }
//
//  def apply[InputType<:ClientToServerMessage[OutputType]:HasMapping, OutputType](clientToServerMessage: InputType)(callback: OutputType=>Unit) = {
//    val id = IDGenerator.genID()
//    AjaxClient[Api].sendToServer(clientToServerMessage).call().onComplete {
//      case Failure(exception) => {
//        println(
//          s"""No answer from server to :$clientToServerMessage
//            |Exception: $exception""".stripMargin)
//      }
//      case Success(serverAnswer) =>
//        CallbackForServerMessages.callbackForServerMessages(serverAnswer)
//    }
//  }
//
//
//}

//object ClientServerExchanger {
//  object IDGenerator {
//    private var id : Long = 0
//    def genID() = {
//      id+=1
//      id
//    }
//  }
//
//  trait HasMapping[InputType<:ClientToServerMessage[OutputType], OutputType] {
//    private val callbackMapping = scala.collection.mutable.Map[Long, (OutputType => Unit)]()
//    def addMapping(id: Long, callback: OutputType=>Unit) = {
//      callbackMapping += (id -> callback)
//    }
//    def getCallback(id: Long) = {
//      callbackMapping.get(id).get
//    }
//  }
//
//  implicit object submitSourceCode extends HasMapping[SubmitSourceCode, SourceCodeSubmissionResultNetwork]
//
//  def apply[InputType<:ClientToServerMessage[OutputType]:HasMapping, OutputType](clientToServerMessage: InputType)(callback: OutputType=>Unit) = {
//    val id = IDGenerator.genID()
////    register the callback for this id in the HasMapping of the type of clientToServerMessage
//    clientToServerMessage.
//    AjaxClient[Api].request(ClientToServerMessages_withID(id, clientToServerMessage)).call().onComplete {
//      case Failure(exception) => {
//        println(
//          s"""No answer from server to :$clientToServerMessage
//              |Exception: $exception""".stripMargin)
//        //    remove the callback for the id in the HasMapping of the type of clientToServerMessage
//      }
//      case Success(serverAnswer) =>
//        this.receiveMessageFromServer(serverAnswer)
//    }
//  }
//
//  def receiveMessageFromServer(serverAnswer: ServerAnswer) = {
//    val id = serverAnswer.id
//    val callback =
//  }
//}

object ClientServerMessager {
  object IDGenerator {
    private var id : Long = 0
    def genID() = {
      id+=1
      id
    }
  }

  private val callbackMap = scala.collection.mutable.Map[Long, CallbackForClientToServerMessage_Abstract]()

  def callServer[ServerAnswerType](functionCall: FunctionCall[ServerAnswerType])(callback: ServerAnswerType => Unit) = {
//    When you expect an answer
    val id = IDGenerator.genID()
    val messageWithID = ClientToServerMessages_withID(id, functionCall)
    val callbackForStorage = CallbackForClientToServerMessage(callback)
    callbackMap += id -> callbackForStorage
    AjaxClient[Api].sendToServer(messageWithID).call().onComplete {
      case Failure(exception) => {
        println(
          s"""No answer from server to :$functionCall
              |Exception: $exception""".stripMargin)
        //    remove the callback for the id in the HasMapping of the type of clientToServerMessage
        callbackMap -= id
      }
      case Success(serverMessage) =>
        this.receiveFromServer(serverMessage)
    }
  }

  def messageServer(clientToServerMessage: ClientToServerMessage) = {
//    When you don't expect an answer
    val id = IDGenerator.genID()
    val messageWithID = ClientToServerMessages_withID(id, clientToServerMessage)
    AjaxClient[Api].sendToServer(messageWithID).call().onComplete {
      case Failure(exception) => {
        println(
          s"""No answer from server to :$clientToServerMessage
              |Exception: $exception""".stripMargin)
        //    remove the callback for the id in the HasMapping of the type of clientToServerMessage
      }
      case Success(serverMessage) =>
        this.receiveFromServer(serverMessage)
    }
  }


//  def sendToServer[ServerAnswerType](clientToServerMessage: ClientToServerMessage[ServerAnswerType])(callback: ServerAnswerType => Unit = ()) = {
//    val id = IDGenerator.genID()
//    val clientToServerMessage_withID = ClientToServerMessages_withID(id, clientToServerMessage)
//    val callbackForStorage = CallbackForClientToServerMessage[ServerAnswerType](callback)
//    callbackMap += id -> callbackForStorage
//    AjaxClient[Api].sendToServer(clientToServerMessage_withID).call().onComplete {
//      case Failure(exception) => {
//        println(
//          s"""No answer from server to :$clientToServerMessage
//              |Exception: $exception""".stripMargin)
//        //    remove the callback for the id in the HasMapping of the type of clientToServerMessage
//        callbackMap -= id
//      }
//      case Success(serverMessage) =>
//        this.receiveFromServer(serverMessage)
//    }
//  }

  private def receiveFromServer(wrappedMessage: ServerToClientMessage_withID) = {
    wrappedMessage match {
      case ServerToClientMessage_withID(id, answer) =>
        answer match {
          case fr: FunctionReturn =>
            val callback = callbackMap(id)
            callback.applyCallback(fr.returnValue)
            callbackMap -= id
        }
    }
  }

}
