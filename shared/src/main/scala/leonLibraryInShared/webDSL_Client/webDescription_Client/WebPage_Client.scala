package leonLibraryInShared.webDSL_Client.webDescription_Client

/**
  * Created by dupriez on 3/1/16.
  */
//TODO: Check if a WebPage is pickable by boopickle. I remember that boopickle can only pickle case classes, and inheritance of case classes is prohibited
sealed trait WebPage_Client

case object ErrorWebPage_Client extends WebPage_Client

case class BlankWebPage_Client(sons: List[WebAttribute_Client]) extends WebPage_Client