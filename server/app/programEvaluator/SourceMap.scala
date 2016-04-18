package programEvaluator

import leon.purescala.Expressions.Expr
import leon.webDSL.webDescription.WebElement
import logging.OptionValWithLog
import shared.IDGenerator

/**
  * Created by dupriez on 3/31/16.
  */
class SourceMap(val sourceCode: String){
  private val _webElementIDToExpr: scala.collection.mutable.Map[Int, Expr] = scala.collection.mutable.Map()
  def webElementIDToExpr(webElementID: Int) : OptionValWithLog[Expr] = {
    if (_webElementIDToExpr.contains(webElementID)) {
      println("key found in sourceMap")
      OptionValWithLog(Some(_webElementIDToExpr(webElementID)), "SourceMap query succesful")
    }
    else {
      println("key not found in sourceMap")
      OptionValWithLog(None, "SourceMap query for webElementID: \"" + webElementID + "\" failed")
    }
  }
//  def addMapping(webElementID: Int, webElementExpr: Expr) = {
//    _webElementIDToExpr(webElementID) = webElementExpr
//  }
//  val idGenerator = new IDGenerator
//  def addWebElementExpr(webElementExpr: Expr) = {
//    _webElementIDToExpr(idGenerator.generateID()) = webElementExpr
//  }
  def addMapping(id: Int, exprOfUneavaluatedWebElement: Expr) = {
    _webElementIDToExpr(id) = exprOfUneavaluatedWebElement
  }
}


