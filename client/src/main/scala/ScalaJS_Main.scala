import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.Timer

import japgolly.scalajs.react.{Callback, CallbackTo, ReactDOM, ReactElement}
import org.scalajs.dom
import dom.{Element, document}
import shared._
import leon.webDSL.webDescription._
import leon.lang.Map._

import scalatags.JsDom.all._
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, ScalaJSDefined}
import js.Dynamic.{literal => l}
import org.scalajs.jquery.{JQuery, JQueryAjaxSettings, JQueryEventObject, JQueryXHR, jQuery => $}

import scala.util.{Failure, Success}
import com.scalawarrior.scalajs.ace._
import japgolly.scalajs.react.vdom.prefix_<^._

import scala.collection.mutable.ListBuffer

/** Imports for using AjaxClient **/
import scala.concurrent.ExecutionContext.Implicits.global
import boopickle.Default._
import boopickle.PicklerHelper
import autowire._
import scala.language.implicitConversions
import japgolly.scalajs.react.ReactNode
/** **/

object ScalaJS_Main extends js.JSApp {
  import shared.Picklers._

//  An attribute that SHOULD NOT be used by the end user, whose purpose is to serve as id for the html elements of the web interface
  val reservedAttributeForImplicitWebProgrammingID = "data-reservedattributeforimplicitwebprogrammingid".reactAttr
  val reservedAttributeForImplicitWebProgrammingID_name = "data-reservedattributeforimplicitwebprogrammingid"

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

  def submitSourceCode() = {
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
                  """.stripMargin)
//            webPage.asInstanceOf[WebPageWithIDedWebElements].sons.foldLeft(0)((useless, webElem) => {println(webElem.weid); useless})
//            dom.document.getElementById("sourceCodeSubmitButton").setAttribute("style", "background-color:none")
            SourceCodeSubmitButton.removeCustomBackground()
            renderWebPage(webPage, "htmlDisplayerDiv")
          }
          case SourceCodeSubmissionResult(None, log) => {
            println("Received \"None\" while expecting \"Some(WebPage)\" from the server")
          }
        }
      }
    }
  }

  def submitStringModification(stringModification: StringModification) = {
    println(
      s"""Send String Modification:
        |WebElementID: ${stringModification.webElementID}
        |ModifiedWebAttribute: ${stringModification.modifiedWebAttribute}
        |NewValue: ${stringModification.newValue}
      """.stripMargin)
    AjaxClient[Api].submitStringModification(stringModification).call().onComplete {
      case Failure(exception) => {println("error during submission of modification: " + exception)}
      case Success(stringModificationSubmissionResult) => {
        println("Server sent something in response to a string modification submission")
        stringModificationSubmissionResult match {
          case StringModificationSubmissionResult(Some((newSourceCode, webPageWithIDedWebElements)), log) => {
//            println(
//              s"""
//                 |Received new source code: $newSourceCode
//                  """.stripMargin)
            println(
              s"""
                 |Received new source code: TEMPORARY DISABLED
                  """.stripMargin)
            renderWebPage(webPageWithIDedWebElements, "htmlDisplayerDiv")
//            remove the standard onChange callback of the Ace Editor, so that the "submit source code change" button does not turn red
//            because of the following call to AceEditor.setEditorValue
            AceEditor.removeAceEdOnChangeCallback()
            AceEditor.setEditorValue(newSourceCode)
            AceEditor.activateAceEdOnChangeCallback_standard()
          }
          case StringModificationSubmissionResult(None, log) => {
            println("Received \"None\" while expecting \"Some(newSourceCode)\" from the server")
          }
        }
      }
    }
  }

  def getElementByImplicitWebProgrammingID(impWebProgID: String) : org.scalajs.jquery.JQuery = {
//    $("["+reservedAttributeForImplicitWebProgrammingID_name+"="+impWebProgID+"]")
    println("getElementByImplicitWebProgrammingID, on ID: "+impWebProgID)
    println("jquery request: "+"["+reservedAttributeForImplicitWebProgrammingID_name+"="+impWebProgID+"]")
//    val res = dom.document.querySelector("["+reservedAttributeForImplicitWebProgrammingID_name+"="+impWebProgID+"]")
    val res = $("["+reservedAttributeForImplicitWebProgrammingID_name+"="+impWebProgID+"]")
//    $("[data-reservedattributeforimplicitwebprogrammingid="+impWebProgID+"]")
    println("end of getElementByImplicitWebProgrammingID")
    res
  }

  object SourceCodeSubmitButton {
    private val idOfThis = "sourceCodeSubmitButton"
    val scalaJSButton = <.button(
      reservedAttributeForImplicitWebProgrammingID := idOfThis,
      ^.className := "btn btn-default",
      ^.onClick --> Callback{submitSourceCode()},
      "Run code"
    )
    private def getTheJSObject = {
//      dom.document.querySelector("["+reservedAttributeForImplicitWebProgrammingID_name+"="+idOfThis+"]")
      $("["+reservedAttributeForImplicitWebProgrammingID_name+"="+idOfThis+"]")
    }
    def turnBackgroundRed() = {
//      getTheJSObject.setAttribute("style", "background-color:red")
      getTheJSObject.removeClass("btn-default")
      getTheJSObject.addClass("btn-primary")
    }
    def removeCustomBackground() = {
//      getTheJSObject.setAttribute("style", "background-color:none")
      getTheJSObject.removeClass("btn-primary")
      getTheJSObject.addClass("btn-default")
    }
  }

//  The following is to allow the
  @ScalaJSDefined
  trait AugmentedElement extends Element {
    val value: js.UndefOr[String]
    val innerText: js.UndefOr[String]
    val text: js.UndefOr[String]
//    val textContent: js.UndefOr[String]
//    val textContent: String
  }
  implicit def elementAugmentation(e: Element): AugmentedElement ={
    e.asInstanceOf[AugmentedElement]
  }

  def fillSourceCodeDiv() = {
    val destinationDivId = "SourceCodeDiv"

    val title = <.h1("Behind the scenes")

    def submitHtmlButtonCallback = Callback{
        println("submit html change")
    }
    val submitHtmlButton = <.button(
      ^.className := "btn btn-secondary",
      ^.onClick --> submitHtmlButtonCallback,
      "Submit html change"
    )
    def stringModificationForm() = {
      val idField = <.input(
        reservedAttributeForImplicitWebProgrammingID := "idField_stringModificationForm",
        ^.`type` := "text", ^.name := "webEID", ^.placeholder := "1")
      val attributeField = <.input(
        reservedAttributeForImplicitWebProgrammingID := "attributeField_stringModificationForm",
        ^.`type` := "checkbox", ^.name := "attribute", ^.value := "Text", ^.placeholder := "Text")
      val attributeFieldLabel = "Text"
      val newValueField = <.input(
        reservedAttributeForImplicitWebProgrammingID := "newValueField_stringModificationForm",
        ^.`type` := "text", ^.name := "newValue", ^.placeholder := "newValue")

      /*def webAttributeNameToStringWebAttribute(waName: String) : Option[WebAttribute]= {
        waName match {
          case "Text" => Some(Text)
          case _ =>
            println("attribute name (\""+waName+"\")not recognised as a StringWebAttribute in function webAttributeNameToWebAttribute")
            None
        }
      }*/
      def submitButtonCallback = Callback{
          println("Submit string modification button clicked")
          var abort = false
//          val weID : Int = ("0" + dom.document.getElementById("idField_stringModificationForm").value.getOrElse("")).toInt
          val weID : Int = ("0" + getElementByImplicitWebProgrammingID("idField_stringModificationForm").value.getOrElse("")).toInt
//          println("weID=" + weID)
//          val webAttributeName = dom.document.getElementById("attributeField_stringModificationForm").getAttribute("value")
          val webAttributeName: String = getElementByImplicitWebProgrammingID("attributeField_stringModificationForm").attr("value")
          val webAttribute =
            if (webAttributeName == "Text") None
            else Some(webAttributeName)
//          val newValue : String = dom.document.getElementById("newValueField_stringModificationForm").value.getOrElse("")
          val newValue : String = getElementByImplicitWebProgrammingID("newValueField_stringModificationForm").text()
//        println("newValue= "+ newValue)
          if(!abort) submitStringModification(StringModification(weID, webAttribute, newValue))

      }

      val submitStringModificationButton =
        <.button(
            "Submit String Modification",
            ^.className := "btn btn-primary",
            ^.onClick --> submitButtonCallback)

      <.div(
        idField,
        attributeField,
        attributeFieldLabel,
        newValueField,
        submitStringModificationButton
      )
    }
    val menuHtml = <.div(
      ^.id := "htmlMenu",
      stringModificationForm(),
      submitHtmlButton
    )

    val submitCodeButton = SourceCodeSubmitButton.scalaJSButton

    val aceEditorDiv = <.div(
      ^.id := "aceeditor"//,
      //^.fontSize := "12px",
      //^.position := "absolute",
      //^.width := "50%",
      //^.top := 130,
      //^.right := 0,
      //^.bottom := 0,
      //^.left := 0
    )
    
    val minimizeButton = <.div("<< minimize",
        ^.id := "minimizeButton"
    )

    val divContent = <.div(
      minimizeButton,
      title,
      aceEditorDiv,
      submitCodeButton,
      menuHtml
    )
    ReactDOM.render(divContent, document.getElementById(destinationDivId))
    $("#minimizeButton").on("click", () => {
      minimizeSourceCodeView()
    })
    AceEditor.initialiseAndIncludeEditorInWebPage()
  }
  
  def minimizeSourceCodeView(): Unit = {
    $("#SourceCodeDiv").animate(l(left = "-600px"), complete = () => {
      $("#ViewerDiv").removeClass("edited")
      $("#minimizeButton").text("Behind the scenes >>")
      val w = $("#minimizeButton").width()
      $("#minimizeButton").css("right", "-" + (w + 20) + "px").off("click").on("click", () => {
        $("#SourceCodeDiv").animate(l(left = "0px"), complete = () => {
          $("#ViewerDiv").addClass("edited")
          $("#minimizeButton").text("<< minimize").css("right", "10px").off("click").on("click", () => {
            minimizeSourceCodeView()
          })
        })
      })
    })
  }

  def fillViewerDiv() = {
    val destinationDivId = "ViewerDiv"
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
              setEditorValue(bootstrapSourceCode)
              editor.getSession().on("change", aceEdOnChangeCallbackVal_master)
//              editor.getSession().on("change", DoNothing_OnChangeCallback.onChangeCallback)
              activateAceEdOnChangeCallback_standard()
              submitSourceCode()
            case Right(serverError) =>
              println("ajax bootstrap source code request failed: It triggered the following server error: "+serverError.text)
          }


      }


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

    def setEditorValue(value: String) = {
      aceEditor match {
        case Some(e) =>
//          println("Setting Ace Editor value to: " + value)
          e.setValue(value)
          e.selection.clearSelection()
        case None => "[ERROR] fun setEditorValue was called while there was no aceEditor"
      }
    }

    def removeAceEdOnChangeCallback() = {
      currentOnChangeCallback = DoNothing_OnChangeCallback.onChangeCallback
//      currentOnChangeCallback = DoNothing_OnChangeCallback
    }

    def activateAceEdOnChangeCallback_standard() = {
      currentOnChangeCallback = Standard_OnChangeCallback.onChangeCallback
//      currentOnChangeCallback = Standard_OnChangeCallback
    }

//    @JSExport
//    def setSourceCodeRequestWillBeRemoved() = {
//      AjaxClient[Api].setSourceCode(getEditorValue).call().onComplete{
//        case Failure(exception) => {println("setSourceCode request failed: " + exception.getMessage)}
//        case Success(unit) => {println("setSourceCode request successful")}
//      }
//    }

    private trait OnChangeCallback {val onChangeCallback : js.Function1[scala.scalajs.js.Any, Unit]}

    private var currentOnChangeCallback/*: OnChangeCallback */= Standard_OnChangeCallback.onChangeCallback
    private val aceEdOnChangeCallbackVal_master/*: js.Function1[scala.scalajs.js.Any, Unit]*/ = aceEdOnChangeCallback_master _
    private def aceEdOnChangeCallback_master(uselessThingJustThereForTypingWithJavaScriptFunctions: scala.scalajs.js.Any) : Unit = {
      currentOnChangeCallback("useless")
//      println("masterCallback: currentOnChangeCallback=" +currentOnChangeCallback)
//      if(currentOnChangeCallback == Standard_OnChangeCallback) {
//        aceEdOnChangeCallback_standard("useless")
//      }
//      else{
//        aceEdOnChangeCallback_doNothing("useless")
//      }
    }

    private case object Standard_OnChangeCallback extends OnChangeCallback {override val onChangeCallback: js.Function1[scala.scalajs.js.Any, Unit] = aceEdOnChangeCallback_standard _}
    private def aceEdOnChangeCallback_standard(uselessThingJustThereForTypingWithJavaScriptFunctions: scala.scalajs.js.Any) : Unit= {
//      println("AceEditor onChange callback: standard")
      SourceCodeSubmitButton.turnBackgroundRed()
//      dom.document.getElementById("sourceCodeSubmitButton").setAttribute("style", "background-color:red")
//      AjaxClient[Api].setSourceCode(getEditorValue).call().onComplete{
//        case Failure(exception) => {println("setSourceCode request failed: " + exception.getMessage)}
//        case Success(unit) => {println("setSourceCode request successful")}
//      }
//      setSourceCodeRequestsAbsorber.newRequest()
    }

    private case object DoNothing_OnChangeCallback extends OnChangeCallback {override val onChangeCallback: js.Function1[scala.scalajs.js.Any, Unit] = aceEdOnChangeCallback_doNothing _}
    private def aceEdOnChangeCallback_doNothing(uselessThingJustThereForTypingWithJavaScriptFunctions: scala.scalajs.js.Any) : Unit = {
//      Do Nothing
//      println("AceEditor onChange callback: do nothing")
  }
  }

  object StringModificationSubmitter {
//  After the edition of a string by the user, the client will wait 'delay' ms before sending the StringModification to the server
//  The delay will be reinitialised after each modification To THE SAME ATTRIBUTE OF THE SAME ELEMENT!
//  (so modifications to other webElements/webAttributes done during the delay WILL NOT BE TAKEN INTO ACCOUNT)
    private val delay = 1000
    private var lastModification : StringModification = null
    private var timeout: Option[Int] = None
    private def stopTimeout() = {println("stop Text modification timeout"); timeout.foreach(to => dom.window.clearTimeout(to))}
    private def launchTimeout(stringModification: StringModification) = {println("launch Text modification timeout"); timeout = Some(dom.setTimeout(()=>{lastModification=null; submitStringModification(stringModification)}, delay))}
    private def buildStringModification(webElementID: Int): StringModification = {
//      println("WebElem with ID 7: "+getElementByImplicitWebProgrammingID("7")(0).innerText)
//      val newValue = getElementByImplicitWebProgrammingID("7")(0).innerText.get /*match {*/
//      println("textContent of webElem with ID 7: "+getElementByImplicitWebProgrammingID("7")(0).textContent)
      println("innerText of webElem with ID 7: "+getElementByImplicitWebProgrammingID("7")(0).innerText)
//      println("text of webElem with ID 7: "+getElementByImplicitWebProgrammingID("7")(0).text)
//      val newValue = getElementByImplicitWebProgrammingID("7")(0).innerText.get /*match {*/
//      val newValue = getElementByImplicitWebProgrammingID("7")(0).text.getOrElse("getOrElseFailed") /*match {*/
      val newValue = getElementByImplicitWebProgrammingID("7")(0).innerText.getOrElse("getOrElseFailed") /*match {*/
//  case Some(string) => string
//  case None =>
//    print("ERROR: StringModificationSubmitter was unable to get the innerText of the webElement of impliciWebProgrammingID: "+webElementID)
//    throw new RuntimeException("ERROR: StringModificationSubmitter was unable to get the innerText of the webElement of impliciWebProgrammingID: "+webElementID)
//}
      //      val newValue = getElementByImplicitWebProgrammingID(webElementID.toString).text()

      StringModification(webElementID, None, newValue)
    }
    def newStringModificationOfTheTextWebAttr(webElementID: Int) = {
      if (lastModification != null) {
        if (lastModification.webElementID == webElementID) {
//          This new modification is on the same webElement and on the same WebAttribute than the current one.
          lastModification = buildStringModification(webElementID)
          stopTimeout()
          launchTimeout(lastModification)
        }
      }
      else {
        lastModification = buildStringModification(webElementID)
        launchTimeout(lastModification)
      }
    }
  }

//  def includeScriptInMainTemplate(scriptTagToInclude: scalatags.JsDom.TypedTag[org.scalajs.dom.html.Script]) = {
//    dom.document.getElementById("scalajsScriptInclusionPoint").appendChild(scriptTagToInclude.render)
//  }
  def renderWebPage(webPageWithIDedWebElements: WebPageWithIDedWebElements, destinationDivID: String) = {
    val webPageDiv = <.div(
      ^.id := "webPage",
      convertWebElementWithIDToReactElement(webPageWithIDedWebElements.main)
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
    *
    * @param webElWithID
    * @return
    */
  def convertWebElementWithIDToReactElement(webElWithID: WebElement) : ReactNode = {
//    val webElID = /*idGenerator.generateID()*/ 0
    def generateTextChangeCallback(webElID: Int) = {
      Callback{
        StringModificationSubmitter.newStringModificationOfTheTextWebAttr(webElID)
      }
    }

    def splitTextIntoReactNodeSeq(text: String): Seq[ReactNode] = {
      text.split("(?=\n)").flatMap( (element: String) =>
        if(element.startsWith("\n")) {
          List[ReactNode](
            <.br,
            element.substring(1) : ReactNode
          )
        }
        else if(element == "") {
         List[ReactNode]()
        }
        else {
          List[ReactNode](element: ReactNode)
        }
      )
    }
//    val textChangeCallBack = Callback{
//      StringModificationSubmitter.newStringModificationOfTheTextWebAttr(webElID)
//    }
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
//<<<<<<< HEAD
//          case Input(/*webElID,*/tpe, placeHolder, text) =>
//            <.input(^.tpe := tpe, ^.placeholder := placeHolder, ^.value := text)
//          case Header(/*webElID,*/ text, level) =>
//            val textChangeCallback = generateTextChangeCallback(webElID)
//            level match {
//              case HLOne() =>
//                <.h1(
//                  text,
//                  reservedAttributeForImplicitWebProgrammingID := webElID,
//                  ^.title := "webElID= "+webElID,
//                  ^.contentEditable := "true",
//                  ^.onChange --> textChangeCallback,
//                  ^.onInput --> textChangeCallback
//                )
//              case HLTwo() =>
//                <.h2(
//                  text,
//                  reservedAttributeForImplicitWebProgrammingID := webElID,
//                  ^.title := "webElID= "+webElID,
//                  ^.contentEditable := "true",
//                  ^.onChange --> textChangeCallback,
//                  ^.onInput --> textChangeCallback
//                )
//              case HLThree() =>
//                <.h3(
//                  text,
//                  reservedAttributeForImplicitWebProgrammingID := webElID,
//                  ^.title := "webElID= "+webElID,
//                                    ^.contentEditable := "true",
//                  ^.onChange --> textChangeCallback,
//                  ^.onInput --> textChangeCallback
//                )
//              case HLFour() =>
//                <.h4(
//                  text,
//                  reservedAttributeForImplicitWebProgrammingID := webElID,
//                  ^.title := "webElID= "+webElID,
//                                    ^.contentEditable := "true",
//                  ^.onChange --> textChangeCallback,
//                  ^.onInput --> textChangeCallback
//                )
//              case HLFive() =>
//                <.h5(
//                  text,
//                  reservedAttributeForImplicitWebProgrammingID := webElID,
//                  ^.title := "webElID= "+webElID,
//                                    ^.contentEditable := "true",
//                  ^.onChange --> textChangeCallback,
//                  ^.onInput --> textChangeCallback
//                )
//              case HLSix() =>
//                <.h6(
//                  text,
//                  reservedAttributeForImplicitWebProgrammingID := webElID,
//                  ^.title := "webElID= "+webElID,
//                                    ^.contentEditable := "true",
//                  ^.onChange --> textChangeCallback,
//                  ^.onInput --> textChangeCallback
//                )
//            }
//          case Paragraph(/*webElID,*/ text) =>
//            val textChangeCallback = generateTextChangeCallback(webElID)
//            val p = <.p(
//              text,
//              reservedAttributeForImplicitWebProgrammingID := webElID,
//              ^.contentEditable := "true",
//              ^.onChange --> textChangeCallback,
//              ^.onInput --> textChangeCallback,
          case TextElement(text) =>
            val textChangeCallback = generateTextChangeCallback(webElID)
            <.span(splitTextIntoReactNodeSeq(text),
              reservedAttributeForImplicitWebProgrammingID := webElID,
              ^.contentEditable := "true",
              ^.onChange --> textChangeCallback,
              ^.onInput --> textChangeCallback,
              ^.title := "webElID= "+webElID
            )
          case Element(tag, sons, attributes, styles) =>
//            print("I'm a div being processed by convertWebElementWithIDToReactElement. I'm about to apply convertWebElementWithIDToReactElement on each element of the list: " + sons)
            tag.reactTag(
              reservedAttributeForImplicitWebProgrammingID := webElID,
              leonListToList(sons).map(convertWebElementWithIDToReactElement),
              ^.title := "webElID= "+webElID,
              leonListToList(attributes).map{ x => x.attributeName.reactAttr := x.attributeValue },
              leonListToList(styles).map{ x => x.attributeName.reactStyle := x.attributeValue }
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