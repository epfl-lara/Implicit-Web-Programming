package programEvaluator

import leon.purescala.Expressions.Expr
import leon.webDSL.webDescription.WebElement
import logging.OptionValWithLog

/**
  * Created by dupriez on 3/31/16.
  */
class SourceMap(val sourceCode: String){
  private val _webElementIDToExpr: scala.collection.mutable.Map[Int, Expr] = scala.collection.mutable.Map()
  def webElementIDToExpr(webElementID: Int) : OptionValWithLog[Expr] = {
    if (_webElementIDToExpr.contains(webElementID)) {
      OptionValWithLog(Some(_webElementIDToExpr(webElementID)), "SourceMap query succesful")
    }
    else {
      OptionValWithLog(None, "SourceMap query for webElementID: \"" + webElementID + "\" failed")
    }
  }
  def addMapping(id: Int, exprOfUneavaluatedWebElement: Expr) = {
    _webElementIDToExpr(id) = exprOfUneavaluatedWebElement
  }
}


