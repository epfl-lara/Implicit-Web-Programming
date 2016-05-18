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
object Server {
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
  
  def ![T <: MessageFromServer](msg: MessageToServerExpecting[T], callback: PartialFunction[T, Unit]): Unit = {
    val storedCallback = callback.asInstanceOf[PartialFunction[U forSome {type U <: MessageFromServer}, Unit]]
    callbacks += storedCallback
    val msgToSend: MessageToServer = msg
    AjaxClient[Api].sendToServer(msgToSend).call().onComplete {
      case Failure(exception) => {
        println(
          s"""No answer from server to :$msg
              |Exception: $exception""".stripMargin)
        callbacks -= storedCallback
      }
      case Success(serverMessage) =>
        receive(serverMessage)
    }
  }
}
