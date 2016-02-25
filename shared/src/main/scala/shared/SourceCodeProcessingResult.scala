package shared

/**
  * Created by dupriez on 2/25/16.
  */
final case class SourceCodeProcessingResult(successOrNot: Boolean, errorStringIfAny: Option[String], generatedHtml: Option[TheTypeOfTheGeneratedHtml] )
