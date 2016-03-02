package shared.webpageBuildingDSL

import java.io.File

/**
  * Created by dupriez on 3/1/16.
  */
object WebpageBuildingDSLFilesPathsProvider {
  //TODO: make this list compute itself automatically
  private val fileNamesList = List(
    "WebElement.scala",
    "WebPage.scala",
    "WebAttribute.scala"
  )
  private val relativePathToWebpageBuildingDSLPackage = "shared/src/main/scala/shared/webpageBuildingDSL"
  private val nameOfThisClass = this.getClass.getSimpleName.dropRight(1)+".scala"
  private val fileNamesListBis = {
    val webpagebuildingDSLFolder = new File(relativePathToWebpageBuildingDSLPackage)
    webpagebuildingDSLFolder.listFiles().filter(file => file.getName != nameOfThisClass)
  }
  fileNamesListBis.foreach(file => println(file.getName))

  val relativePathsToWebpageBuildingDSLFiles = fileNamesList.map(fileName => relativePathToWebpageBuildingDSLPackage + "/" + fileName)
}
