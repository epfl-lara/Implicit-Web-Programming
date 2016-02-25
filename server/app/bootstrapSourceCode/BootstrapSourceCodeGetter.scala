package bootstrapSourceCode

import java.io.ByteArrayInputStream

import serverReporter.{Error, Info, ServerReporter}

import scala.io.BufferedSource

/**
  * Created by dupriez on 2/25/16.
  */
object BootstrapSourceCodeGetter {
  val pathToBootstrapSourceCodeFile = "server/app/bootstrapSourceCode/BootstrapSourceCode"

  def getBootstrapSourceCode(serverReporter: ServerReporter): String = {
    //    val pathToLeonInput = "src/main/scala-2.11/trash.manipulatedFiles/SourceCode.txt"
    serverReporter.report(Info, "Serving SourceCode...")
    var error = false
    val bootstrapSourceCodeFile: BufferedSource = try {
      scala.io.Source.fromFile(pathToBootstrapSourceCodeFile)
    } catch {
      case e: java.io.FileNotFoundException => {
        //        mainReporter.error("File opening error, \"" + pathToLeonInput + "\" not found")
        serverReporter.report(Error, "ERROR: File opening error, \"" + pathToBootstrapSourceCodeFile + "\" not found, \":-(\" sent back to caller")
        error = true
        new BufferedSource(new ByteArrayInputStream(Array(':', '-', '(')))
      }
    }
    if (!error) {
      serverReporter.report(Info, "BootstrapSourceCode file found")
    }
    else {
      serverReporter.report(Info, "Error")
    }
    val bootstrapSourceCode = bootstrapSourceCodeFile.mkString
    bootstrapSourceCodeFile.close
    bootstrapSourceCode
  }
}
