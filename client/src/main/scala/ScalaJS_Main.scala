import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer

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
      AceEditor.initialiseAndIncludeEditorInWebPage()


    /**Request tests*/
//    AjaxClient[Api].getSourceCodeBis().call().onComplete {
//      case Failure(exception) => {println("getSourceCodBis failed")}
//      case Success(dummy) => {println("getSourceCodBis succeeded " + dummy)}
//    }
//    AjaxClient[Api].sendAndGetBackInt(5).call().foreach((i:Int) => println("Sent and Get Back Int"))
//    AjaxClient[Api].getFive().call().onSuccess {
//      case i => println(i)
//    }

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

  def fetchAndUseSourceCode(whatToDoWithSourceCode: String => Unit) = {
//    var sourceCode = ""
    AjaxClient[Api].getSourceCode().call().onComplete {
      case Failure(exception) => {println("Unable to fetch source code: " + exception.getMessage)}
      case Success(fetchedsourceCode) => {println("ajax source code request success"); /*sourceCode = */whatToDoWithSourceCode(fetchedsourceCode)}
    }
//    println(sourceCode)
//    println("sourceCode should have been printed (in function getSourceCode of ScalaJS_Main)")
//    sourceCode
  }

  object AceEditor {
    //ID of the html div that should contain the aceeditor
    val aceEditorID = "aceeditor"
    //Contains the aceEditor created
    var aceEditor: Option[Editor] = None

    def initialiseAndIncludeEditorInWebPage() = {
      val editor = ace.edit(aceEditorID)
      aceEditor = Some(editor)
      editor.setTheme("ace/theme/monokai")
      editor.getSession().setMode("ace/mode/scala")
      editor.getSession().setTabSize(2)
      updateEditorContent()
      val changeCallbackFunction: js.Function1[scala.scalajs.js.Any, Unit] = aceEditorChangeCallback _
      editor.getSession().on("change", changeCallbackFunction)

      //    val sourceCode = getSourceCode()
      //    println(sourceCode)
      //    println("sourceCode should have been printed (in function initialiseAndIncludeAceEditorForSourceCode of ScalaJS_Main)")
      //    editor.setValue(sourceCode)
    }

    def updateEditorContent() = {
      aceEditor match {
        case Some(editor) => fetchAndUseSourceCode(s => editor.setValue(s))
        case None => println("Strange, someone asked to update the aceEditor while there is none")
      }
    }

    def getEditorValue = {
      aceEditor match {
        case Some(e) => e.getValue()
        case None => "[ERROR] fun getEditorValue was called while there was no aceEditor"
      }
    }

//    private object setSourceCodeRequestsAbsorber {
//      private val idleTimeNeededBeforeRequestTransmission_ds = 25
//
//      def newRequest() = {
//        timeUntilNextRequest_ds = idleTimeNeededBeforeRequestTransmission_ds
//      }
//
////      private val timeUntilNextRequestDecreaseTimer = new Timer(1, new ActionListener() {
////        @Override
////        def actionPerformed(arg0: ActionEvent) {
////          if(timeUntilNextRequest_ds > 0) {
////            timeUntilNextRequest_ds = timeUntilNextRequest_ds - 1
////            if(idleTimeNeededBeforeRequestTransmission_ds == 0) {
////              sendRequest()
////            }
////          }
////        }
////      })
////      timeUntilNextRequestDecreaseTimer.setRepeats(true)
////      timeUntilNextRequestDecreaseTimer.start()
//
////      org.scalajs.dom.setInterval( () => {
////        println("absorber timer executed")
////        if(timeUntilNextRequest_ds > 0) {
////          timeUntilNextRequest_ds = idleTimeNeededBeforeRequestTransmission_ds - 1
////          if(timeUntilNextRequest_ds == 0) {
////            sendRequest()
////          }
////        }
////
////      }, 100)
//
//      private var timeUntilNextRequest_ds = 0
//
//      private def sendRequest() = {
//        AjaxClient[Api].setSourceCode(getEditorValue).call().onComplete{
//          case Failure(exception) => {println("setSourceCode request failed: " + exception.getMessage)}
//          case Success(unit) => {println("setSourceCode request successful")}
//        }
//      }
//    }

    private def aceEditorChangeCallback(uselessThingJustThereForTypingWithJavaScriptFunctions: scala.scalajs.js.Any) : Unit= {
      println("ace change callback")
//      AjaxClient[Api].setSourceCode(getEditorValue).call().onComplete{
//        case Failure(exception) => {println("setSourceCode request failed: " + exception.getMessage)}
//        case Success(unit) => {println("setSourceCode request successful")}
//      }
//      setSourceCodeRequestsAbsorber.newRequest()
    }
  }

  def includeScriptInMainTemplate(scriptTagToInclude: scalatags.JsDom.TypedTag[org.scalajs.dom.html.Script]) = {
    dom.document.getElementById("scalajsScriptInclusionPoint").appendChild(scriptTagToInclude.render)
  }
}