package shared
import leon.webDSL.webDescription.{WebSiteWithIDedContent}

/**
  * Created by dupriez on 3/10/16.
  */
case class StringModificationSubmissionResult(newSourceCodeAndWebSite: Option[(String, WebSiteWithIDedContent)], log: String)

case class StringModificationSubmissionResultForNetwork(stringModificationSubmissionResult: StringModificationSubmissionResult, stringModSubResID: Int)