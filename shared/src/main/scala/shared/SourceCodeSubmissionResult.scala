package shared

import leon.webDSL.webDescription.{WebPage, WebSiteWithIDedContent}

/**
  * Created by dupriez on 3/10/16.
  */
case class SourceCodeSubmissionResult(webSiteWithIDedContentOption: Option[WebSiteWithIDedContent], evaluationLog: String)
