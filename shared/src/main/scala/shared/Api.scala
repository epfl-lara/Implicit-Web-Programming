package shared

/**
  * Created by dupriez on 2/12/16.
  */
trait Api {
  def getSourceCode(): String
  def setSourceCode(newSourceCode: String): Unit

  def getSourceCodeBis(): String
  def sendAndGetBackInt(i: Int): Int
  def getFive(): Int
}
