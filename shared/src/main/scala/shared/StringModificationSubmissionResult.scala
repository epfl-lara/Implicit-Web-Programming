package shared
import leon.webDSL.webDescription.{WebPageWithIDedWebElements}

/**
  * Created by dupriez on 3/10/16.
  */
case class StringModificationSubmissionResult(newSourceCodeAndWebPage: Option[(String, WebPageWithIDedWebElements)], log: String)
