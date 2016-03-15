package shared

//import leonLibraryInShared.webDSL_Client.webDescription_Client.WebPage
import leon.webDSL.webDescription._
/**
  * Created by dupriez on 2/25/16.
  * The reason why this is not a hierarchy of classes is because boopickle cannot pickle non-case classes, and case-to-case inheritance is not allowed in scala
  */
final case class SourceCodeProcessingResult(generatedHtml: WebPage )
