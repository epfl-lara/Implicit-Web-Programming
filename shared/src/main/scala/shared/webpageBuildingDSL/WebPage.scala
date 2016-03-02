package shared.webpageBuildingDSL

/**
  * Created by dupriez on 3/1/16.
  */
//TODO: Check if a WebPage is pickable by boopickable. I remember that boopickle can only pickle case classes, and inheritance of case classes is prohibited
sealed trait WebPage {
  val content: List[WebElement] = List()

  val testString = "Coucou"
}

case object ErrorWebPage extends WebPage {

}

case object BlankWebPage extends WebPage {

}
