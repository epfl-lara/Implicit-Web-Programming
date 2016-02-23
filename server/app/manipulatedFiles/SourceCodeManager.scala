package manipulatedFiles

import java.io._
import javax.print.attribute.standard.Severity

import serverReporter._

import scala.io.BufferedSource

/**
  * Created by dupriez on 2/15/16.
  */
object SourceCodeManager {
  val pathToManipulatedFilesFolder = "server/app/manipulatedFiles"
  val pathFromManipulatedFilesFolderToSourceCodeFile = "SourceCode.txt"

  def pathToSourceCodeFile = pathToManipulatedFilesFolder + "/" + pathFromManipulatedFilesFolderToSourceCodeFile
//  val pathToLeonInput = "server/app/manipulatedFiles/SourceCode.txt"

  def getSourceCode(serverReporter: ServerReporter): String = {
    //    val pathToLeonInput = "src/main/scala-2.11/manipulatedFiles/SourceCode.txt"
    serverReporter.report(Info, "Serving SourceCode...")
    var error = false
    val leonInputFile: BufferedSource = try {
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
    val leonInput = leonInputFile.mkString
    leonInputFile.close
    leonInput

  }

  def rewriteSourceCode(newSourceCode: String, serverReporter: ServerReporter): Unit = {
//    FileWriter try
    serverReporter.report(Info, "fun rewriteSourceCode starting with argument:\n"+newSourceCode)
    val file = new File(pathToSourceCodeFile)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(newSourceCode)
    bw.close()
    serverReporter.report(Info, "fun rewriteSourceCode returning")

    //    val pw = new PrintWriter(new File("hello.txt" ))
//    pw.write("Hello, world")
//    pw.close
//
//    serverReporter.report(Info, "sourceCode rewritten")
  }
}
