import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer

import japgolly.scalajs.react.{Callback, CallbackTo, ReactDOM, ReactElement}
import org.scalajs.dom
import dom.{Element, document}
import shared._
import leon.webDSL.webDescription._
import leon.lang.Map._
import webDSL.webDescription._

import scalatags.JsDom.all._
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, ScalaJSDefined}
import scala.util.{Failure, Success}
import com.scalawarrior.scalajs.ace._
import japgolly.scalajs.react.vdom.prefix_<^._

/** Imports for using AjaxClient **/
import scala.concurrent.ExecutionContext.Implicits.global
import boopickle.Default._
import boopickle.PicklerHelper
import autowire._
/** **/

object ScalaJS_Main extends js.JSApp {
  import shared.Picklers._

  def main(): Unit = {
    //    println("method main of ScalaJSExample is running")
    //    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
    //    displaySourceCode()
    //    includeScriptInMainTemplate(script("console.info(\"hey\")"))
    fillSourceCodeDiv()
    fillViewerDiv()
    /*TODO: Intention: the client would make an automatic code submission of the bootstrap code right after being loaded.
      TODO: In practice, it seems to sends an empty string as sourcecode to the server
    submitButtonAction() */
  }

  def submitButtonAction() = {
    println("submit source code change")
    AjaxClient[Api].submitSourceCode(AceEditor.getEditorValue).call().onComplete {
      case Failure(exception) => {println("error during submission of the source code: " + exception)}
      case Success(sourceCodeProcessingResult) => {
        println("Server sent something in response to a code submission")
        sourceCodeProcessingResult match {
          case SourceCodeSubmissionResult(Some(webPage), log) => {
            println(
              s"""
                 |Received "Some(WebPage)"
                 |  Number of webPageAttributes: ${webPage.webPageAttributes.size}
                 |  Number of webElements: ${webPage.sons.size}
                  """.stripMargin)
//            webPage.asInstanceOf[WebPageWithIDedWebElements].sons.foldLeft(0)((useless, webElem) => {println(webElem.weid); useless})
            renderWebPage(webPage, "htmlDisplayerDiv")
          }
          case SourceCodeSubmissionResult(None, log) => {
            println("Received \"None\" while expecting \"Some(WebPage)\" from the server")
          }
        }
      }
    }
  }
  def submitButtonCallback = Callback{
      submitButtonAction()
  }

  def submitStringModification(webElementID: Int, modifiedAttribute: StringWebAttribute, newValue: String) = {
    println("Send Modification")
    AjaxClient[Api].submitStringModification(StringModification(webElementID, modifiedAttribute, newValue)).call().onComplete {
      case Failure(exception) => {println("error during submission of modification: " + exception)}
      case Success(stringModificationSubmissionResult) => {
        println("Server sent something in response to a string modification submission")
        stringModificationSubmissionResult match {
          case StringModificationSubmissionResult(Some(newSourceCode), log) => {
            println(
              s"""
                 |Received new source code: $newSourceCode
                  """.stripMargin)
//            renderWebPage(webPage, "htmlDisplayerDiv")
          }
          case StringModificationSubmissionResult(None, log) => {
            println("Received \"None\" while expecting \"Some(newSourceCode)\" from the server")
          }
        }
      }
    }
  }

  def fillSourceCodeDiv() = {
    val destinationDivId = "SourceCodeDiv"

    val title = <.h1("Source Code")


    val submitButton = <.button(
    ^.onClick --> submitButtonCallback,
    "Submit source code change"
    )

    val aceEditorDiv = <.div(
      ^.id := "aceeditor",
      ^.fontSize := "12px",
      ^.position := "absolute",
      ^.width := "50%",
      ^.top := 130,
      ^.right := 0,
      ^.bottom := 0,
      ^.left := 0
    )

    val divContent = <.div(
      title,
      submitButton,
      aceEditorDiv
    )
    ReactDOM.render(divContent, document.getElementById(destinationDivId))
    AceEditor.initialiseAndIncludeEditorInWebPage()
  }

  def fillViewerDiv() = {
    val destinationDivId = "ViewerDiv"

    val title = <.h1("Viewer")
    def submitButtonCallback = Callback{
        println("submit html change")
    }
    val submitButton = <.button(
      ^.onClick --> submitButtonCallback,
      "Submit html change"
    )
    def stringModificationForm() = {
      val idField = <.input(^.id := "idField_stringModificationForm", ^.`type` := "text", ^.name := "webEID", ^.placeholder := "1")
      val attributeField = <.input(^.id := "attributeField_stringModificationForm", ^.`type` := "radio", ^.name := "attribute", ^.value := "Text", ^.placeholder := "Text")
      val attributeFieldLabel = "Text"
      val newValueField = <.input(^.id := "newValueField_stringModificationForm", ^.`type` := "text", ^.name := "newValue", ^.placeholder := "newValue")

      def webAttributeNameToStringWebAttribute(waName: String) : Option[StringWebAttribute]= {
        waName match {
          case "Text" => Some(Text)
          case _ =>
            println("attribute name (\""+waName+"\")not recognised as a StringWebAttribute in function webAttributeNameToWebAttribute")
            None
        }
      }
      @ScalaJSDefined
      trait AugmentedElement extends Element {
        val value: js.UndefOr[String]
      }
      implicit def elementAugmentation(e: Element): AugmentedElement ={
        e.asInstanceOf[AugmentedElement]
      }
      def submitButtonCallback = Callback{
          println("Submit string modification button clicked")
          var abort = false
          val weID : Int = ("0" + dom.document.getElementById("idField_stringModificationForm").value.getOrElse("")).toInt
//          println("weID=" + weID)
          val webAttributeName = dom.document.getElementById("attributeField_stringModificationForm").getAttribute("value")
          val webAttribute = webAttributeNameToStringWebAttribute(webAttributeName) match {
            case Some(webA) => webA
            case None =>
              println("Error before sending a string modification to the server: modified WebAttribute could not be obtained from the name:\""+webAttributeName+"\"")
              abort = true
              Text
          }
          val newValue : String = dom.document.getElementById("newValueField_stringModificationForm").value.getOrElse("")
//        println("newValue= "+ newValue)
          if(!abort) submitStringModification(weID, webAttribute, newValue)
      }

      val submitStringModificationButton = <.button("Submit String Modification", ^.onClick --> submitButtonCallback)

      <.div(
        idField,
        attributeField,
        attributeFieldLabel,
        newValueField,
        submitStringModificationButton
      )
    }
//    val f = stringModificationForm.

//    var i = 1
//    def testCallback = Callback{
//        println("testCallback")
//        val par = <.p(
//          "paragraph " + i
//        )
//        ReactDOM.render(par, document.getElementById("htmlDisplayerDiv"))
//        i=i+1
//        println("paragraph rendered")
//    }
//    val testButton = <.button(
//      ^.onClick --> testCallback,
//      "Test button"
//    )
    val htmlDisplayerDiv = <.div(
      ^.id := "htmlDisplayerDiv"
    )
    val divContent = <.div(
      title,
      stringModificationForm(),
      submitButton,
//      testButton,
      htmlDisplayerDiv
    )
    ReactDOM.render(divContent, document.getElementById(destinationDivId))
  }

//  def fetchAndUseSourceCode(whatToDoWithSourceCode: String => Unit) = {
////    var sourceCode = ""
//    AjaxClient[Api].getSourceCode().call().onComplete {
//      case Failure(exception) => {println("Unable to fetch source code: " + exception.getMessage)}
//      case Success(fetchedsourceCode) => {println("ajax source code request success"); /*sourceCode = */whatToDoWithSourceCode(fetchedsourceCode)}
//    }
////    println(sourceCode)
////    println("sourceCode should have been printed (in function getSourceCode of ScalaJS_Main)")
////    sourceCode
//  }

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
//      updateEditorContent()
      AjaxClient[Api].getBootstrapSourceCode().call().onComplete {
        case Failure(exception) => {println("Unable to fetch bootstrap source code: " + exception.getMessage)}
        case Success(serverReturn) =>
          serverReturn match {
            case Left(bootstrapSourceCode) =>
              println("ajax bootstrap source code request success")
              editor.setValue(bootstrapSourceCode)
            case Right(serverError) =>
              println("ajax bootstrap source code request failed: It triggered the following server error: "+serverError.text)
          }


      }
      val changeCallbackFunction: js.Function1[scala.scalajs.js.Any, Unit] = aceEditorChangeCallback _
      editor.getSession().on("change", changeCallbackFunction)

      //    val sourceCode = getSourceCode()
      //    println(sourceCode)
      //    println("sourceCode should have been printed (in function initialiseAndIncludeAceEditorForSourceCode of ScalaJS_Main)")
      //    editor.setValue(sourceCode)
    }

//    def updateEditorContent() = {
//      aceEditor match {
//        case Some(editor) => fetchAndUseSourceCode(s => editor.setValue(s))
//        case None => println("Strange, someone asked to update the aceEditor while there is none")
//      }
//    }

    def getEditorValue = {
      aceEditor match {
        case Some(e) => e.getValue()
        case None => "[ERROR] fun getEditorValue was called while there was no aceEditor"
      }
    }

//    @JSExport
//    def setSourceCodeRequestWillBeRemoved() = {
//      AjaxClient[Api].setSourceCode(getEditorValue).call().onComplete{
//        case Failure(exception) => {println("setSourceCode request failed: " + exception.getMessage)}
//        case Success(unit) => {println("setSourceCode request successful")}
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

//  def includeScriptInMainTemplate(scriptTagToInclude: scalatags.JsDom.TypedTag[org.scalajs.dom.html.Script]) = {
//    dom.document.getElementById("scalajsScriptInclusionPoint").appendChild(scriptTagToInclude.render)
//  }
  def renderWebPage(webPageWithIDedWebElements: WebPageWithIDedWebElements, destinationDivID: String) = {
    val webPageDiv = <.div(
      ^.id := "webPage",
      leonListToList(webPageWithIDedWebElements.sons).map(convertWebElementWithIDToReactElement)
    )
    ReactDOM.render(webPageDiv, document.getElementById(destinationDivID))
  }

  def leonListToList[T](leonList: leon.collection.List[T]): List[T] = {
    val listBuffer = leonList.foldLeft(scala.collection.mutable.ListBuffer[T]())((list, elem)=>list += elem)
    listBuffer.toList
  }

//  an attribute to store the webElement IDs attributed during the execution of the program
  val impWebProgIDAttr = "data-impwebprogid".reactAttr

  /**
    * This function should only be given WebElementWithID
    * @param webElWithID
    * @return
    */
  def convertWebElementWithIDToReactElement(webElWithID: WebElement /*, idGenerator: IDGenerator*/) : ReactElement = {
//    val webElID = /*idGenerator.generateID()*/ 0
    webElWithID match {
      case WebElementWithID(webElem, webElID) =>
        webElem match {
    //      case Header(level, stringAttributes) =>
    //        val text : String = stringAttributes.get(Text) match {
    //          case leon.lang.Some(string) => string
    //          case leon.lang.None() => ""
    //        }
    //        level match {
    //          case HLOne() => <.h1(text)
    //          case HLTwo() => <.h2(text)
    //          case HLThree() => <.h3(text)
    //          case HLFour() => <.h4(text)
    //          case HLFive() => <.h5(text)
    //          case HLSix() => <.h6(text)
    //        }
    //      case Paragraph(stringAttributes) =>
    //        val text : String = stringAttributes.get(Text) match {
    //          case leon.lang.Some(string) => string
    //          case leon.lang.None() => ""
    //        }
    //        <.p(text)
          case Header(/*webElID,*/ text, level) =>
            level match {
              case HLOne() => <.h1(text, impWebProgIDAttr := webElID, ^.title := "webElID= "+webElID)
              case HLTwo() => <.h2(text, impWebProgIDAttr := webElID, ^.title := "webElID= "+webElID)
              case HLThree() => <.h3(text, impWebProgIDAttr := webElID, ^.title := "webElID= "+webElID)
              case HLFour() => <.h4(text, impWebProgIDAttr := webElID, ^.title := "webElID= "+webElID)
              case HLFive() => <.h5(text, impWebProgIDAttr := webElID, ^.title := "webElID= "+webElID)
              case HLSix() => <.h6(text, impWebProgIDAttr := webElID, ^.title := "webElID= "+webElID)
            }
          case Paragraph(/*webElID,*/ text) =>
            //TODO: change "heyhey" to the value of the text of the paragraph
            val textChangeCallBack = Callback{ println("paragraph changed"); submitStringModification(webElID, Text, "heyhey")}
            val p = <.p(
              text,
              impWebProgIDAttr := webElID,
//              ^.contentEditable := "true",
              ^.onChange --> textChangeCallBack,
              ^.onInput --> textChangeCallBack,
              ^.title := "webElID= "+webElID
            )
            p
          case Div(/*webElID,*/ sons) =>
//            print("I'm a div being processed by convertWebElementWithIDToReactElement. I'm about to apply convertWebElementWithIDToReactElement on each element of the list: " + sons)
            <.div(
              impWebProgIDAttr := webElID,
              leonListToList(sons).map(convertWebElementWithIDToReactElement),
              ^.title := "webElID= "+webElID
            )
          case WebElementWithID(_,_) =>
    //        Should never happen
            println("Erreur: convertWebElementToReactElement was given a WebElementWithID (good) wrapping a WebElementWithID (bad)")
            <.p()
        }
      case _ =>
//        Not supposed to happen, since convertWebElementToReactElement should only be given webElementWithID
        println(
          s"""Erreur: convertWebElementToReactElement was given something else than a WebElementWithID:
             |  argument: $webElWithID
           """.stripMargin)
        <.p()
    }
  }

}
