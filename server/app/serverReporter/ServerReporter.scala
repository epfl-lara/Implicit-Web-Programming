package serverReporter

import scala.collection.mutable

/**
  * Collect the messages it is given
  */
class ServerReporter {
  private val messageQueue = mutable.Queue[SRMessage]()

  def report(severity: SRSeverity, rawMessage: String): Unit = {
    messageQueue.enqueue(SRMessage(rawMessage, severity))
  }

  def flushMessageQueue(whatToDoWithTheOutputString: String=>Unit) = {
    val outputString = messageQueue.foldLeft("")((acc, msg)=> acc + msg.messageToString + sys.props("line.separator"))
    whatToDoWithTheOutputString(outputString)
  }
}
