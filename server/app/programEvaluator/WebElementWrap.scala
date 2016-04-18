package programEvaluator

import leon.webDSL.webDescription.WebElement

/**
  * Created by dupriez on 4/6/16.
  * The purpose of this class is to defeat the default equality of the WebElement case classes
  */
class WebElementWrap(val webElem: WebElement)
object WebElementWrapper {
  private val previousWrappings = scala.collection.mutable.Map[WebElement, WebElementWrap]()
  def wrap(webElem: WebElement): WebElementWrap = {
    ???
  }
}