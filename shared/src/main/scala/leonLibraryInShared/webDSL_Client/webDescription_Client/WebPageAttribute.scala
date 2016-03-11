package leonLibraryInShared.webDSL_Client.webDescription_Client

/**
  * Created by dupriez on 3/11/16.
  */
sealed trait WebPageAttribute

case class TestWebPageAttribute1(oi: Int) extends WebPageAttribute
