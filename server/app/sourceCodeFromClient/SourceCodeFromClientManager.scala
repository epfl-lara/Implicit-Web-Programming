package sourceCodeFromClient

import java.io._
import javax.print.attribute.standard.Severity

import serverReporter._

import scala.io.BufferedSource

/**
  * Created by dupriez on 2/29/16.
  */
object SourceCodeFromClientManager {
  val pathToManipulatedFilesFolder = "server/app/sourceCodeFromClient"
  val pathFromManipulatedFilesFolderToSourceCodeFile = "SourceCodeFromClient.txt"

  def pathToSourceCodeFile = pathToManipulatedFilesFolder + "/" + pathFromManipulatedFilesFolderToSourceCodeFile

  def getSourceCode(serverReporter: ServerReporter): String = {
    serverReporter.report(Info, "Serving SourceCode...")
    var error = false
    val sourceCodeFile: BufferedSource = try {
      scala.io.Source.fromFile(pathToSourceCodeFile)
    } catch {
      case e: java.io.FileNotFoundException => {
        //        mainReporter.error("File opening error, \"" + pathToLeonInput + "\" not found")
        serverReporter.report(Error, "ERROR: File opening error, \"" + pathToSourceCodeFile + "\" not found, \":-(\" sent back to caller")
        error = true
        new BufferedSource(new ByteArrayInputStream(Array(':', '-', '(')))
      }
    }
    if (!error) {
      serverReporter.report(Info, "SourceCode file found")
    }
    else {
      serverReporter.report(Info, "Error")
    }
    val SourceCode = sourceCodeFile.mkString
    sourceCodeFile.close
    SourceCode

  }

  def rewriteSourceCode(newSourceCode: String, serverReporter: ServerReporter): Unit = {
    serverReporter.report(Info, "fun rewriteSourceCode starting with argument:\n"+newSourceCode)
    val file = new File(pathToSourceCodeFile)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(newSourceCode)
    bw.close()
    serverReporter.report(Info, "fun rewriteSourceCode returning")
  }
}