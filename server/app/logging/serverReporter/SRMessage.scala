package logging.serverReporter

/**
  * Created by dupriez on 2/15/16.
  */
//"SR" -> "Server Reporter"
private case class SRMessage(rawMessage: String, severity: SRSeverity) {
  def messageToString = severity.severityString + " " + rawMessage
}
