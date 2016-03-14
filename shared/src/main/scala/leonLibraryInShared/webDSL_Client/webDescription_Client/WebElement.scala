package leonLibraryInShared.webDSL_Client.webDescription_Client

import boopickle.Default._

/**
  * Created by dupriez on 3/11/16.
  *
  * All new subclasses of WebElement must also be registered in the pickler (see the companion object)
  */
sealed trait WebElement

case class TestWebElement1(sons: Seq[WebElement]) extends WebElement

object WebElement {
  implicit val webElementPickler = compositePickler[WebElement]
  webElementPickler.addConcreteType[TestWebElement1]
}
