package shared

/**
  * Created by dupriez on 2/12/16.
  */
trait Api {
  def getBootstrapSourceCode(): Either[String, ServerError]
  def submitSourceCode(sourceCode: String): SourceCodeSubmissionResult
//  def submitHtml(don't know, something that indicate a change made to the html): String //New Source Code
  def submitStringModification(stringModificationForNetwork: StringModificationForNetwork): StringModificationSubmissionResultForNetwork
}
