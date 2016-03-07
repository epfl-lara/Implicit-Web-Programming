package shared.webpageBuildingDSL

import java.io.File
import java.io.File
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.mutable.ListBuffer

/**
  * Created by dupriez on 3/1/16.
  * The val "relativePathToWebpageBuildingDSLPackage" has to be maintained correct.
  * This file has to be kept at the root of the "shared.webpageBuildingDSL" package.
  */
object WebpageBuildingDSLFilesPathsProvider {
  private val relativePathToWebpageBuildingDSLPackage = "shared/src/main/scala/shared/webpageBuildingDSL"

  private val nameOfTheFileOfThisClass = this.getClass.getSimpleName.dropRight(1)+".scala"
  private val relativePathToTheFileOfThisClass = relativePathToWebpageBuildingDSLPackage + "/" + nameOfTheFileOfThisClass
  private val relativePathToTheWarningFileOfTheWebpageBuildingDSLPackage = relativePathToWebpageBuildingDSLPackage + "/" + "Warning.txt"

  val relativePathsToWebpageBuildingDSLFiles: ListBuffer[String] = {
    val l: ListBuffer[String]= ListBuffer()
    FileListingVisitor.doTheVisit(relativePathToWebpageBuildingDSLPackage, pathToFile => {l += pathToFile.toString})
    l.filter(string => (string != relativePathToTheFileOfThisClass) && (string != relativePathToTheWarningFileOfTheWebpageBuildingDSLPackage))
  }

  def importLine = {
    "import shared.webpageBuildingDSL._"
  }
}

//Converted from java code (http://www.javapractices.com/topic/TopicAction.do?Id=68)
private object FileListingVisitor {
  def doTheVisit(root: String, whatToDoWithThePathsToFiles: Path => Unit){
    val fileProcessor = new ProcessFile(whatToDoWithThePathsToFiles)
    Files.walkFileTree(Paths.get(root), fileProcessor)
  }

  private final class ProcessFile(whatToDoWithThePathsToFiles: Path => Unit) extends SimpleFileVisitor[Path] {
    override def visitFile(aFile: Path, aAttrs: BasicFileAttributes): FileVisitResult = {
//      println("Processing file:" + aFile)
      whatToDoWithThePathsToFiles(aFile)
      return FileVisitResult.CONTINUE
    }

    override def preVisitDirectory(aDir: Path, aAttrs: BasicFileAttributes): FileVisitResult = {
//      println("Processing directory:" + aDir)
      return FileVisitResult.CONTINUE
    }
  }
}
