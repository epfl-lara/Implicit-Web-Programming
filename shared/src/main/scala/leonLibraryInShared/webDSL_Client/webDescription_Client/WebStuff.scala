package leonLibraryInShared.webDSL_Client.webDescription_Client

//import boopickle.Default._
/**
  * Created by dupriez on 3/4/16.
  */

sealed trait WebStuff
sealed trait WebAttribute extends WebStuff
case class TestWebAttribute(oi: Int) extends WebAttribute
case class WebElement(attributes: List[WebAttribute], sons: List[WebElement]) extends WebStuff


//object WebStuff {
//  implicit val pickler: Pickler[WebStuff] = generatePickler[WebStuff]
//}