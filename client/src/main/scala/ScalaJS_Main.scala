import org.scalajs.dom
import dom.document
import shared.Api

import scalatags.JsDom.all. _

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

import com.scalawarrior.scalajs.ace._

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
//    includeScriptInMainTemplate(script("console.info(\"hey\")"))
    initialiseAndIncludeAceEditorForSourceCode()
//    displaySourceCode()
  }

//  def displaySourceCode() = {
//    AjaxClient[Api].getSourceCode().call().onComplete {
//      case Failure(exception) => println("Unable to fetch source code: " + exception.getMessage)
////      case Success(sourceCode) => dom.document.getElementById("editor").innerHTML = sourceCode
//      case Success(sourceCode) => includeScriptInMainTemplate(script("editor.setValue(\"" + sourceCode + "\")"))
//    }
//  }

//  def displaySourceCode() = {
//    AjaxClient[Api].getSourceCode().call().onComplete {
//      case Failure(exception) => println("Unable to fetch source code: " + exception.getMessage)
////      case Success(sourceCode) => dom.document.getElementById("editor").innerHTML = sourceCode
//      case Success(sourceCode) => dom.document.getElementById("editor").setValue("hey")
//    }
//  }

  def getSourceCode() = {
    var sourceCode = ""
    AjaxClient[Api].getSourceCode().call().onComplete {
      case Failure(exception) => println("Unable to fetch source code: " + exception.getMessage)
      case Success(fetchedsourceCode) => sourceCode = fetchedsourceCode
    }
    sourceCode
  }

  def initialiseAndIncludeAceEditorForSourceCode() = {
    val aceEditorID = "aceeditor"
    val editor = ace.edit(aceEditorID)
    editor.setTheme("ace/theme/monokai")
    editor.getSession().setMode("ace/mode/scala")
    editor.getSession().setTabSize(2)
    val changeCallbackFunction: js.Function1[scala.scalajs.js.Any, Unit] = aceEditorChangeCallback _
    editor.getSession().on("change", changeCallbackFunction)

    val sourceCode = getSourceCode()
    println(sourceCode)
    println("sourceCode should have been printed")
    editor.setValue(sourceCode)
  }

  def includeScriptInMainTemplate(scriptTagToInclude: scalatags.JsDom.TypedTag[org.scalajs.dom.html.Script]) = {
    dom.document.getElementById("scalajsScriptInclusionPoint").appendChild(scriptTagToInclude.render)
  }

  def aceEditorChangeCallback(useless: scala.scalajs.js.Any) = {
    println("ace change callback")
  }
}