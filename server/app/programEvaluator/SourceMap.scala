package programEvaluator

import leon.purescala.Expressions.Expr

/**
  * Created by dupriez on 3/31/16.
  */
class SourceMap(val sourceCode: String){
  private val _webElementIDToExpr: scala.collection.mutable.Map[Int, Expr] = scala.collection.mutable.Map()
  def webElementIDToExpr : Map[Int, Expr] = _webElementIDToExpr.toMap
  def addMapping(webElementID: Int, webElementExpr: Expr) = {
    _webElementIDToExpr(webElementID) = webElementExpr
  }
}
