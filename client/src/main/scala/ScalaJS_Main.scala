import org.scalajs.dom
import dom.document
import shared.Api

import scalatags.JsDom.all. _

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/** Imports for using AjaxClient **/
import scala.concurrent.ExecutionContext.Implicits.global
import boopickle.Default._
import autowire._
/** **/

object ScalaJS_Main extends js.JSApp {
  def main(): Unit = {
//    println("method main of ScalaJSExample is running")
//    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
//    displaySourceCode()
    includeScriptInMainTemplate(script("console.info(\"hey\")"))
  }

  def displaySourceCode() = {
    AjaxClient[Api].getSourceCode().call().onComplete {
      case Failure(exception) => println("Unable to fetch source code: " + exception.getMessage)
      case Success(sourceCode) => dom.document.getElementById("sourceCode").innerHTML = sourceCode
    }
  }

  def includeACEEditorTriggerOnChange() = {

  }

  def includeScriptInMainTemplate(scriptTagToInclude: scalatags.JsDom.TypedTag[org.scalajs.dom.html.Script]) = {
    dom.document.getElementById("scalajsScriptInclusionPoint").appendChild(scriptTagToInclude.render)
  }

  @JSExport
  def printInConsole() = {
    println("printInConsole")
  }

}