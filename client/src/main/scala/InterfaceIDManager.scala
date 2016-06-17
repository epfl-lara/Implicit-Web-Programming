import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.jquery.{jQuery => $}
/**
  * Created by dupriez on 5/23/16.
  *
  * The purpose of this object is to ease the use of the "interface id".
  * The "interface id" is a custom html attributes, whose purpose is to store values that will act as ids for the components of the web interface.
  * The reason for having this alternative id as a custom attribute instead of simply using the "id" field are twofolds:
  * - Making it less likely that a script defined by the user to interact with his webpage (if we ever add this functionalities)
  *   will interact with the interface elements
  * -
  */
object InterfaceIDManager {
  private val complicatedString = "reservedattributeforimplicitwebprogramminginterfaceid"

  val interfaceIDAttr = ("data-"+complicatedString).reactAttr
  def getInterfaceElementFromID(understandableID: String) : org.scalajs.jquery.JQuery = {
    //    $("["+reservedAttributeForImplicitWebProgrammingID_name+"="+impWebProgID+"]")
    println("getInterfaceElementFromID, on ID: "+understandableID)
    println("jquery request: "+"["+complicatedString+"="+understandableID+"]")
    //    val res = dom.document.querySelector("["+reservedAttributeForImplicitWebProgrammingID_name+"="+impWebProgID+"]")
    val res = $("["+complicatedString+"="+understandableID+"]")
    //    $("[data-reservedattributeforimplicitwebprogrammingid="+impWebProgID+"]")
    println("end of getElementByImplicitWebProgrammingID")
    res
  }
}
