package leonLibraryInShared.webDSL_Client.webDescription_Client

import boopickle.Default._

/**
  * Created by dupriez on 3/11/16.
  */
sealed trait WebThingy

sealed trait WebElementThingy extends WebThingy

sealed trait WebAttributeThingy extends WebThingy

case class WebElementThingy1(oi: Int) extends WebElementThingy

case class WebAttributeThingy1(oi: Int) extends WebAttributeThingy

object WebThingy {
  implicit val webElementThingyPickler = compositePickler[WebElementThingy]
  webElementThingyPickler.addConcreteType[WebElementThingy1]

  implicit val webAttributeThingyPickler = compositePickler[WebAttributeThingy]
  webAttributeThingyPickler.addConcreteType[WebAttributeThingy1]

  implicit val webThingyPickler = compositePickler[WebThingy]
  webThingyPickler.join[WebElementThingy].join[WebAttributeThingy]
}
