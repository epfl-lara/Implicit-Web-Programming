package services

import bootstrapSourceCode.BootstrapSourceCodeGetter
import trash.manipulatedFiles.SourceCodeManager
import serverReporter.ServerReporter
import shared.{SourceCodeProcessingResult, Api}

import scala.io.BufferedSource

/**
  * Created by dupriez on 2/12/16.
  */
class ApiService extends Api{
//  override def getSourceCode(): String = {
////    //    val pathToLeonInput = "src/main/scala-2.11/trash.manipulatedFiles/SourceCode.txt"
////    val pathToLeonInput = "server/app/trash.manipulatedFiles/SourceCode.txt"
////    val leonInputFile: BufferedSource = try {
////      scala.io.Source.fromFile(pathToLeonInput)
////    } catch {
////      case e: java.io.FileNotFoundException => {
////        //        mainReporter.error("File opening error, \"" + pathToLeonInput + "\" not found")
////        println("ERROR: File opening error, \"" + pathToLeonInput + "\" not found")
////        sys.exit(1)
////      }
////    }
////    val leonInput = leonInputFile.mkString
////    leonInputFile.close
//////    println("File found")
////    leonInput
//////    "SourceCode placeHolder"
//    val srvReporter = new ServerReporter
//    val sourceCode = SourceCodeManager.getSourceCode(srvReporter)
//    srvReporter.flushMessageQueue((s:String) => println(s))
////    println("APIService:" + sourceCode)
//    sourceCode
////    "APIService: This should be some SourceCode, but right now, I'm debugging"
//  }

//  override def setSourceCode(newSourceCode: String): Unit = {
//    val srvReporter = new ServerReporter
//    SourceCodeManager.rewriteSourceCode(newSourceCode, srvReporter)
//    srvReporter.flushMessageQueue((s:String) => println(s))
//  }
  override def getBootstrapSourceCode(): String = {
  val serverReporter = new ServerReporter
  val bootstrapSourceCode = BootstrapSourceCodeGetter.getBootstrapSourceCode(serverReporter)
  serverReporter.flushMessageQueue(msg => println(msg))
  bootstrapSourceCode
}

  override def submitSourceCode(sourceCode: String): SourceCodeProcessingResult = ???
}
