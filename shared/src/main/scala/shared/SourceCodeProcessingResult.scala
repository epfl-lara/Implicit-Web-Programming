package shared

import shared.webpageBuildingDSL.WebPage

/**
  * Created by dupriez on 2/25/16.
  * The reason why this is not a hierarchy of classes is because boopickle cannot pickle non-case classes, and case-to-case inheritance is not allowed in scala
  */
final case class SourceCodeProcessingResult(success: Boolean, errorStringIfAny: Option[String], generatedHtml: WebPage )
