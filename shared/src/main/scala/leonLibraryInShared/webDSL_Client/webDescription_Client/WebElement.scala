package leonLibraryInShared.webDSL_Client.webDescription_Client

import boopickle.Default._

/**
  * Created by dupriez on 3/11/16.
  */
sealed trait WebElement

case class TestWebElement1(sons: List[WebElement]) extends WebElement

object WebElement {
  implicit val webElementPickler = compositePickler[WebElement]
  webElementPickler.addConcreteType[TestWebElement1]
}
