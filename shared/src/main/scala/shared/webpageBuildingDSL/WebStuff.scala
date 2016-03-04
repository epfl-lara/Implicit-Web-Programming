package shared.webpageBuildingDSL

/**
  * Created by dupriez on 3/4/16.
  */

sealed trait WebStuff

case class WebElement(attributes: List[WebAttribute], sons: List[WebElement]) extends WebStuff

sealed trait WebAttribute extends WebStuff

case class TestWebAttribute(oi: Int) extends WebAttribute

