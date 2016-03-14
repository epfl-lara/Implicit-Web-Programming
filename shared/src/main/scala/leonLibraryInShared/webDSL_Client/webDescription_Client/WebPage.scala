package leonLibraryInShared.webDSL_Client.webDescription_Client

//import boopickle.Default._
/**
  * Created by dupriez on 3/1/16.
  */


case class WebPage(webPageAttributes: List[WebPageAttribute], sons: List[WebElement])

//object WebPage {
//  implicit val pickler: Pickler[WebPage] = generatePickler[WebPage]
//}