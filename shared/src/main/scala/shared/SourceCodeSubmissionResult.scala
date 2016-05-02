package shared

import leon.webDSL.webDescription.{WebPage, WebPageWithIDedWebElements}

case class SourceCodeSubmissionNetwork(source: String, requestId: Int)
/**
  * Created by dupriez on 3/10/16.
  */
case class SourceCodeSubmissionResult(webPageWithIDedWebElementsOpt: Option[WebPageWithIDedWebElements], evaluationLog: String)

case class SourceCodeSubmissionResultNetwork(result: SourceCodeSubmissionResult, requestId: Int)