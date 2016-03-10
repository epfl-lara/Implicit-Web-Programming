package leonLibraryInShared.webDSL_Client.webDescription_Client

/**
  * Created by dupriez on 3/1/16.
  */
//TODO: Check if a WebPage is pickable by boopickle. I remember that boopickle can only pickle case classes, and inheritance of case classes is prohibited
sealed trait WebPage

case object ErrorWebPage extends WebPage

case class BlankWebPage(sons: List[WebAttribute]) extends WebPage