package leonLibraryInShared.webDSL_Client.webDescription_Client

/**
  * Created by dupriez on 3/11/16.
  */
sealed trait WebAttribute

case class TestWebAttribute1(oi: Int) extends WebAttribute
