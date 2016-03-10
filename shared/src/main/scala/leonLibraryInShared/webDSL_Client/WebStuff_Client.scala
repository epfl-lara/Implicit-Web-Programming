package webDSL_Client

//import boopickle.Default._
/**
  * Created by dupriez on 3/4/16.
  */

sealed trait WebStuff_Client
sealed trait WebAttribute_Client extends WebStuff_Client
case class TestWebAttribute_Client(oi: Int) extends WebAttribute_Client
case class WebElement_Client(attributes: List[WebAttribute_Client], sons: List[WebElement_Client]) extends WebStuff_Client


//object WebStuff {
//  implicit val pickler: Pickler[WebStuff] = generatePickler[WebStuff]
//}