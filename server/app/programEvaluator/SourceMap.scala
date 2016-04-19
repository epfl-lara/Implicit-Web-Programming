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

  private def lookupCaseClass(program: Program, caseClassFullName: String, serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    val sReporter = serverReporter.startProcess("Source Map looks up the caseClassDef of: \"" + caseClassFullName+"\"")
    program.lookupCaseClass(caseClassFullName) match {
      case Some(classDef) => OptionValWithLog(Some(classDef), "")
      case None =>
        sReporter.report(Error, "Look up gave no result")
        OptionValWithLog(None, "Failed Source Map lookup for the caseClassDef of: \"" + caseClassFullName+"\"")
    }
  }

  def paragraph_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.Paragraph", serverReporter)
  }
  def header_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.Header", serverReporter)
  }
  def div_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.Div", serverReporter)
  }
  def webPage_webElementCaseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.webDSL.webDescription.WebPage", serverReporter)
  }
  def leonCons_caseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.collection.Cons", serverReporter)
  }
  def leonNil_caseClassDef(serverReporter: ServerReporter): OptionValWithLog[CaseClassDef] = {
    lookupCaseClass(program, "leon.collection.Nil", serverReporter)
  }
}


