package services

import shared.Api

import scala.io.BufferedSource

/**
  * Created by dupriez on 2/12/16.
  */
class ApiService extends Api{
  override def getSourceCode(): String = {
    //    val pathToLeonInput = "src/main/scala-2.11/manipulatedFiles/SourceCode.scala"
    val pathToLeonInput = "server/app/manipulatedFiles/SourceCode.scala"
    val leonInputFile: BufferedSource = try {
      scala.io.Source.fromFile(pathToLeonInput)
    } catch {
      case e: java.io.FileNotFoundException => {
        //        mainReporter.error("File opening error, \"" + pathToLeonInput + "\" not found")
        println("ERROR: File opening error, \"" + pathToLeonInput + "\" not found")
        sys.exit(1)
      }
    }
    val leonInput = leonInputFile.mkString
    leonInputFile.close
//    println("File found")
    leonInput
//    "SourceCode placeHolder"
  }
}
