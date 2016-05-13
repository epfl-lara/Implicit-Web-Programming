import shared._
import scala.concurrent.ExecutionContext.Implicits.global
import autowire._
import boopickle.Default._
import scala.util.{Failure, Success}
import scala.collection.mutable.ListBuffer

import scala.language.existentials

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

object Server {
  /*object IDGenerator {
    private var id : Long = 0
    def genID() = {
      id+=1
      id
    }
  }*/

  val callbacks = ListBuffer[PartialFunction[U forSome {type U <: MessageFromServer }, Unit]]()
  
  private def receive(data: MessageFromServer): Unit = {
    callbacks.find(c => c.isDefinedAt(data)) match {
      case Some(callback) =>
        callbacks -= callback
        callback(data)
        return
      case None =>
    }
  }
  
  import boopickle.Default._
  
  import MessageToServer._
  import MessageFromServer._
  
  def ![T <: MessageFromServer](msg: MessageToServerExpecting[T], callback: PartialFunction[T forSome { type T <: MessageFromServer }, Unit]): Unit = {
    callbacks += callback
    val msgToSend: MessageToServer = msg
    AjaxClient[Api].sendToServer(msgToSend).call().onComplete {
      case Failure(exception) => {
        println(
          s"""No answer from server to :$msg
              |Exception: $exception""".stripMargin)
        //    remove the callback for the id in the HasMapping of the type of clientToServerMessage
        callbacks -= callback
      }
      case Success(serverMessage) =>
        receive(serverMessage)
    }
  }
  /*
  def callServer[ServerAnswerType <: MessageFromServer](functionCall: MessageToServerExpecting[ServerAnswerType])(callback: ServerAnswerType => Unit) = {
    Server ! (functionCall, { case res: ServerAnswerType => callback(res) })
    
    
//    When you expect an answer
    //val id = IDGenerator.genID()
    //val messageWithID = ClientToServerMessages_withID(id, functionCall)
    
    
  }*/

}
