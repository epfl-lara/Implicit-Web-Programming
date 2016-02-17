package manipulatedFiles

import java.io.ByteArrayInputStream
import javax.print.attribute.standard.Severity

import serverReporter._

import scala.io.BufferedSource

/**
  * Created by dupriez on 2/15/16.
  */
object SourceCodeGetter {
  def getSourceCode(serverReporter: ServerReporter): String = {
    //    val pathToLeonInput = "src/main/scala-2.11/manipulatedFiles/SourceCode.scala"
    val pathToLeonInput = "server/app/manipulatedFiles/SourceCode.scala"
    serverReporter.report(Info, "Serving SourceCode...")
    val leonInputFile: BufferedSource = try {
      scala.io.Source.fromFile(pathToLeonInput)
    } catch {
      case e: java.io.FileNotFoundException => {
        //        mainReporter.error("File opening error, \"" + pathToLeonInput + "\" not found")
        serverReporter.report(Error, "ERROR: File opening error, \"" + pathToLeonInput + "\" not found, \":-(\" sent back to caller")
        new BufferedSource(new ByteArrayInputStream(Array(':', '-', '(')))
      }
    }
    val leonInput = leonInputFile.mkString
    leonInputFile.close
    //    println("File found")
    leonInput
    //    "SourceCode placeHolder"
  }
}
