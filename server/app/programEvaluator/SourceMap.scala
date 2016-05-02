package programEvaluator

import leon.purescala.Definitions.{CaseClassDef, Program}
import leon.purescala.Expressions.Expr
import leon.webDSL.webDescription.WebElement
import logging.OptionValWithLog
import logging.serverReporter._

/**
  * Created by dupriez on 3/31/16.
  */
class SourceMap(val sourceCode: String, val program: Program){
  private val _webElementIDToExpr: scala.collection.mutable.Map[Int, (WebElement, Expr)] = scala.collection.mutable.Map()
  def webElementIDToExpr(webElementID: Int) : OptionValWithLog[Expr] = {
    if (_webElementIDToExpr.contains(webElementID)) {
      OptionValWithLog(Some(_webElementIDToExpr(webElementID)._2), "SourceMap query succesful")
    }
    else {
      OptionValWithLog(None, "SourceMap query for webElementID: \"" + webElementID + "\" failed")
    }
  }
  def webElementIDToWebElement(webElementID: Int) : OptionValWithLog[WebElement] = {
    if (_webElementIDToExpr.contains(webElementID)) {
      OptionValWithLog(Some(_webElementIDToExpr(webElementID)._1), "SourceMap query succesful")
    }
    else {
      OptionValWithLog(None, "SourceMap query for webElementID: \"" + webElementID + "\" failed")
    }
  }
  def addMapping(id: Int, webElement: WebElement, exprOfUneavaluatedWebElement: Expr) = {
    _webElementIDToExpr(id) = (webElement, exprOfUneavaluatedWebElement)
  }

  private def lookupCaseClass(program: Program, caseClassFullName: String, serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    val sReporter = serverReporter.startProcess("Source Map looks up the caseClassDef of: \"" + caseClassFullName+"\"")
    program.lookupCaseClass(caseClassFullName) match {
      case Some(classDef) =>
        sReporter.report(Info, "Success")
        OptionValWithLog(Some(classDef), "")
      case None =>
        sReporter.report(Error, "Look up gave no result")
        OptionValWithLog(None, "Failed Source Map lookup for the caseClassDef of: \"" + caseClassFullName+"\"")
    }
  }

  def element_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.Element", serverReporter)
  }
  def textElement_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.TextElement", serverReporter)
  }
  def webAttribute_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.WebAttribute", serverReporter)
  }
  def webStyle_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.WebStyle", serverReporter)
  }
  def webPage_caseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.WebPage", serverReporter)
  }
  def webSite_caseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.WebSite", serverReporter)
  }
  def leonCons_caseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.collection.Cons", serverReporter)
  }
  def leonNil_caseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.collection.Nil", serverReporter)
  }
}


