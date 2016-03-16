package shared

import leon.webDSL.webDescription.{WebPage}

/**
  * Created by dupriez on 3/10/16.
  */
case class SourceCodeSubmissionResult(webPageOpt: Option[WebPage], evaluationLog: String)
