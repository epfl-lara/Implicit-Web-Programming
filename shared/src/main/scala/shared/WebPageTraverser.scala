package shared
import leon.webDSL.webDescription._

/**
  * Created by dupriez on 2/11/16.
  */
object WebPageTraverser {
  def traverseWebPage(webPage: WebPage, webElemFunction: (WebElement => Unit)) = {
    webPage match {
      case WebPage(webPageAttributes, sons) =>
        sons.foldLeft(0)((useless, webElem) => {traverseWebElement(webElem, webElemFunction); useless})
    }
  }
  def traverseWebElement(webElem: WebElement, webElemFunction: (WebElement => Unit)): Unit ={
    webElemFunction(webElem)
    webElem.sons.foldLeft(0)((useless, webElem) => {traverseWebElement(webElem, webElemFunction); useless})
  }
}
