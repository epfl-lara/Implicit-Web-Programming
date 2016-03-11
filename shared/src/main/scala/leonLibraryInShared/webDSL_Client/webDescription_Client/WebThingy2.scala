package leonLibraryInShared.webDSL_Client.webDescription_Client

import boopickle.Default._

/**
  * Created by dupriez on 3/11/16.
  */
sealed trait WebThingy2

case class TestWebThingy2(i: Seq[WebThingy2]) extends WebThingy2

object WebThingy2 {
  implicit val webThingy2Pickler = compositePickler[WebThingy2]
  webThingy2Pickler.addConcreteType[TestWebThingy2]
}

