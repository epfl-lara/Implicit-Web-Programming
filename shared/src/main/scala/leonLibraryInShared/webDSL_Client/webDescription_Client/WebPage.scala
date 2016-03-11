package leonLibraryInShared.webDSL_Client.webDescription_Client

//import boopickle.Default._
/**
  * Created by dupriez on 3/1/16.
  */

//case class WebPage(attributesAndSons: List[WebStuff])
//The following compile without boopicklet complaining about "not able to generate pickler" but it is just for a quick test
case class WebPage(webPageAttributes: List[WebPageAttribute], sons: List[WebThingy2])

//object WebPage {
//  implicit val pickler: Pickler[WebPage] = generatePickler[WebPage]
//}