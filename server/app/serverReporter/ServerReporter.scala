package serverReporter

import scala.collection.mutable

/**
  * Collect the messages it is given
  */
class ServerReporter {
  private val messageQueue = mutable.Queue[SRMessage]()
  var printReportsInConsole = false

  def report(severity: SRSeverity, rawMessage: String): Unit = {
    val srMessage = SRMessage(rawMessage, severity)
    messageQueue.enqueue(srMessage)
    if (printReportsInConsole) {
      println(srMessage.messageToString)
    }
  }

  def flushMessageQueue(whatToDoWithTheOutputString: String=>Unit) = {
    val outputString = messageQueue.foldLeft("")((acc, msg)=> acc + msg.messageToString + sys.props("line.separator"))
    whatToDoWithTheOutputString(outputString)
  }
}
