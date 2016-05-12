package services

import bootstrapSourceCode.BootstrapSourceCodeGetter
import leon.purescala.Definitions.Program
import memory.Memory
import programEvaluator.{LeonProgramMaker, ProgramEvaluator}
import logging.serverReporter.{Info, ServerReporter}
import shared._
import stringModification.StringModificationProcessor

/**
  * Created by dupriez on 2/12/16.
  */
class ApiService(onUserRequest: Boolean = true) extends Api {
  override def sendToServer(clientToServerMessages_withID: ClientToServerMessages_withID) = {
    val messageID = clientToServerMessages_withID.id
    clientToServerMessages_withID.clientToServerMessage match {
      case s@GetBootstrapSourceCode() =>
        val answer = processGetBootstrapSourceCode(s)
        ServerToClientMessage_withID(messageID, GetBootstrapSourceCode_answer(answer))
      case s@SubmitSourceCode(sourceCodeSubmissionNetwork) =>
        val answer = processSubmitSourceCode(s)
        ServerToClientMessage_withID(messageID, SubmitSourceCode_answer(answer))
      case s@SubmitStringModification(stringModificationForNetwork) =>
        val answer = processStringModificationSubmission(s)
        ServerToClientMessage_withID(messageID, SubmitStringModification_answer(answer))
    }
  }

  def processGetBootstrapSourceCode(getBootstrapSourceCode: GetBootstrapSourceCode): Option[String] = {
    val serverReporter = new ServerReporter
    BootstrapSourceCodeGetter.getBootstrapSourceCode(serverReporter)
  }

  def processSubmitSourceCode(submitSourceCode: SubmitSourceCode): SourceCodeSubmissionResultNetwork = {
    val submission = submitSourceCode.sourceCode
    val requestId = submission.requestId
    val sourceCode = submission.source
    val serverReporter = new ServerReporter
    LeonProgramMaker.makeProgram(sourceCode, serverReporter) match {
      case Some(program) =>
        ProgramEvaluator.evaluateAndConvertResult(program, sourceCode, serverReporter) match {
          case (Some((webPageWithIDedWebElement, sourceMapProducer, ctx)), evaluationLog) =>
            if (onUserRequest) {
              Memory.setSourceMap(requestId, sourceMapProducer)(ctx)
            } else {
              Memory.setAutoSourceMap(requestId, sourceMapProducer)(ctx)
            }

            SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(Some(webPageWithIDedWebElement), evaluationLog), requestId)
          case (None, evaluationLog) =>
            Memory.setSourceMap(requestId, () => None)(null)
            SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None,
              s"""
                 |ProgramEvaluator did not manage to evaluate and unexpr the result of the leon program.
                 | Here is the evaluation log: $evaluationLog
              """.stripMargin), requestId)
        }
      case None =>
        SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None, "leon did not manage to create a Program out of the source code"), requestId)
    }
  }

  def processStringModificationSubmission(submitStringModification: SubmitStringModification): StringModificationSubmissionResultForNetwork = {
    val submission = submitStringModification.stringModificationForNetwork
    val sReporter = new ServerReporter
    val stringModification = submission.stringModification
    val stringModID = submission.stringModID
    val sourceCodeId = submission.sourceCodeId
    sReporter.report(Info,
      s"""Received a string modification from the client:
          |  webElementID: ${stringModification.webElementID}
          |  modified WebAttribute${stringModification.modifiedWebAttribute}
          |  new value: ${stringModification.newValue}
          |  id: ${stringModID}
           """.stripMargin
    )
    val weID = stringModification.webElementID
    val weExprFromSourceMap = Memory.getSourceMap(sourceCodeId) match {
      case Some(sourceMap) =>
        sourceMap.webElementIDToExpr(weID)
      case None =>
        throw new Exception(s"Could not find code with sourceCodeId = $sourceCodeId, maybe not up-to-date (last recorded is " + Memory.lastSourceId + ")")
    }

    sReporter.report(Info,
      s"""Here's what has been found in the sourceMap for the webElementID $weID:
          |${weExprFromSourceMap}
           """.stripMargin)

    StringModificationSubmissionResultForNetwork(StringModificationProcessor.process(stringModification, sourceCodeId, sReporter), sourceCodeId, stringModID)
  }
}

//class ApiService(onUserRequest: Boolean = true) extends Api {
//  override def sendToServer(clientToServerMessage: ClientToServerMessage) = {
//    clientToServerMessage match {
//      case GetBootstrapSourceCode() => {
//        val serverReporter = new ServerReporter
//        val bootstrapSourceCode = BootstrapSourceCodeGetter.getBootstrapSourceCode(serverReporter)
//        //    serverReporter.flushMessageQueue(msg => println(msg))
//        val result = bootstrapSourceCode match {
//          case Some(sourceCode) => Left(sourceCode)
//          case None => Right(UnableToFetchBootstrapSourceCode())
//        }
//        GetBootstrapSourceCode_answer(result)
//      }
//      case SubmitSourceCode(submission) => {
//        val requestId = submission.requestId
//        val sourceCode = submission.source
//        val serverReporter = new ServerReporter
//        val result = LeonProgramMaker.makeProgram(sourceCode, serverReporter) match {
//          case Some(program) =>
//            ProgramEvaluator.evaluateAndConvertResult(program, sourceCode, serverReporter) match {
//              case (Some((webPageWithIDedWebElement, sourceMapProducer, ctx)), evaluationLog) =>
//                if(onUserRequest) {
//                  Memory.setSourceMap(requestId, sourceMapProducer)(ctx)
//                } else {
//                  Memory.setAutoSourceMap(requestId, sourceMapProducer)(ctx)
//                }
//
//                SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(Some(webPageWithIDedWebElement), evaluationLog), requestId)
//              case (None, evaluationLog) =>
//                Memory.setSourceMap(requestId, () => None)(null)
//                SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None,
//                  s"""
//                     |ProgramEvaluator did not manage to evaluate and unexpr the result of the leon program.
//                     | Here is the evaluation log: $evaluationLog
//              """.stripMargin), requestId)
//            }
//          case None =>
//            SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None, "leon did not manage to create a Program out of the source code"), requestId)
//        }
//        SubmitSourceCode_answer(result)
//      }
//      case SubmitStringModification(stringModificationForNetwork) => {
//        val sReporter = new ServerReporter
//        val stringModification = stringModificationForNetwork.stringModification
//        val stringModID = stringModificationForNetwork.stringModID
//        val sourceCodeId = stringModificationForNetwork.sourceCodeId
//        sReporter.report(Info,
//        s"""Received a string modification from the client:
//           |  webElementID: ${stringModification.webElementID}
//           |  modified WebAttribute${stringModification.modifiedWebAttribute}
//           |  new value: ${stringModification.newValue}
//           |  id: ${stringModID}
//           """.stripMargin
//        )
//        val weID = stringModification.webElementID
//        val weExprFromSourceMap = Memory.getSourceMap(sourceCodeId) match {
//        case Some(sourceMap) =>
//        sourceMap.webElementIDToExpr(weID)
//        case None =>
//        throw new Exception(s"Could not find code with sourceCodeId = $sourceCodeId, maybe not up-to-date (last recorded is "+Memory.lastSourceId+")")
//        }
//
//        sReporter.report(Info,
//        s"""Here's what has been found in the sourceMap for the webElementID $weID:
//           |${weExprFromSourceMap}
//           """.stripMargin)
//
//        val result = StringModificationSubmissionResultForNetwork(StringModificationProcessor.process(stringModification, sourceCodeId, sReporter), sourceCodeId, stringModID)
//        SubmitStringModification_answer(result)
//      }
//    }
//    }
//  }

// class ApiService(onUserRequest: Boolean = true) extends Api{
//  override def request(clientToServerMessage: ClientToServerMessage) = {
//    clientToServerMessage match {
//      case GetBootstrapSourceCode() => {
//        val serverReporter = new ServerReporter
//        val bootstrapSourceCode = BootstrapSourceCodeGetter.getBootstrapSourceCode(serverReporter)
//        //    serverReporter.flushMessageQueue(msg => println(msg))
//        val result = bootstrapSourceCode match {
//          case Some(sourceCode) => Left(sourceCode)
//          case None => Right(UnableToFetchBootstrapSourceCode())
//        }
//        GetBootstrapSourceCode_answer(result)
//      }
//      case SubmitSourceCode(submission) => {
//        val requestId = submission.requestId
//        val sourceCode = submission.source
//        val serverReporter = new ServerReporter
//        val result = LeonProgramMaker.makeProgram(sourceCode, serverReporter) match {
//          case Some(program) =>
//            ProgramEvaluator.evaluateAndConvertResult(program, sourceCode, serverReporter) match {
//              case (Some((webPageWithIDedWebElement, sourceMapProducer, ctx)), evaluationLog) =>
//                if(onUserRequest) {
//                  Memory.setSourceMap(requestId, sourceMapProducer)(ctx)
//                } else {
//                  Memory.setAutoSourceMap(requestId, sourceMapProducer)(ctx)
//                }
//
//                SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(Some(webPageWithIDedWebElement), evaluationLog), requestId)
//              case (None, evaluationLog) =>
//                Memory.setSourceMap(requestId, () => None)(null)
//                SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None,
//                  s"""
//                     |ProgramEvaluator did not manage to evaluate and unexpr the result of the leon program.
//                     | Here is the evaluation log: $evaluationLog
//              """.stripMargin), requestId)
//            }
//          case None =>
//            SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None, "leon did not manage to create a Program out of the source code"), requestId)
//        }
//        SubmitSourceCode_answer(result)
//      }
//      case SubmitStringModification(stringModificationForNetwork) => {
//        val sReporter = new ServerReporter
//        val stringModification = stringModificationForNetwork.stringModification
//        val stringModID = stringModificationForNetwork.stringModID
//        val sourceCodeId = stringModificationForNetwork.sourceCodeId
//        sReporter.report(Info,
//        s"""Received a string modification from the client:
//           |  webElementID: ${stringModification.webElementID}
//           |  modified WebAttribute${stringModification.modifiedWebAttribute}
//           |  new value: ${stringModification.newValue}
//           |  id: ${stringModID}
//           """.stripMargin
//        )
//        val weID = stringModification.webElementID
//        val weExprFromSourceMap = Memory.getSourceMap(sourceCodeId) match {
//        case Some(sourceMap) =>
//        sourceMap.webElementIDToExpr(weID)
//        case None =>
//        throw new Exception(s"Could not find code with sourceCodeId = $sourceCodeId, maybe not up-to-date (last recorded is "+Memory.lastSourceId+")")
//        }
//
//        sReporter.report(Info,
//        s"""Here's what has been found in the sourceMap for the webElementID $weID:
//           |${weExprFromSourceMap}
//           """.stripMargin)
//
//        val result = StringModificationSubmissionResultForNetwork(StringModificationProcessor.process(stringModification, sourceCodeId, sReporter), sourceCodeId, stringModID)
//        SubmitStringModification_answer(result)
//      }
//    }
//    }
//  }

//#Original ApiService, before the migration to leon web client-server communication model
//class ApiService(onUserRequest: Boolean = true) extends Api{
//  override def getBootstrapSourceCode(): Either[String, ServerError] = {
//    val serverReporter = new ServerReporter
//    val bootstrapSourceCode = BootstrapSourceCodeGetter.getBootstrapSourceCode(serverReporter)
////    serverReporter.flushMessageQueue(msg => println(msg))
//    bootstrapSourceCode match {
//      case Some(sourceCode) => Left(sourceCode)
//      case None => Right(UnableToFetchBootstrapSourceCode())
//    }
//  }
//
//  override def submitSourceCode(submission: SourceCodeSubmissionNetwork): SourceCodeSubmissionResultNetwork = {
//    val requestId = submission.requestId
//    val sourceCode = submission.source
//    val serverReporter = new ServerReporter
//    LeonProgramMaker.makeProgram(sourceCode, serverReporter) match {
//      case Some(program) =>
//        ProgramEvaluator.evaluateAndConvertResult(program, sourceCode, serverReporter) match {
//          case (Some((webPageWithIDedWebElement, sourceMapProducer, ctx)), evaluationLog) =>
//            if(onUserRequest) {
//              Memory.setSourceMap(requestId, sourceMapProducer)(ctx)
//            } else {
//              Memory.setAutoSourceMap(requestId, sourceMapProducer)(ctx)
//            }
//
//            SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(Some(webPageWithIDedWebElement), evaluationLog), requestId)
//          case (None, evaluationLog) =>
//            Memory.setSourceMap(requestId, () => None)(null)
//            SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None,
//              s"""
//                |ProgramEvaluator did not manage to evaluate and unexpr the result of the leon program.
//                | Here is the evaluation log: $evaluationLog
//              """.stripMargin), requestId)
//        }
//      case None =>
//        SourceCodeSubmissionResultNetwork(SourceCodeSubmissionResult(None, "leon did not manage to create a Program out of the source code"), requestId)
//    }
//  }
//
//  //  def submitHtml(don't know, something that indicate a change made to the html): String //New Source Code
//
//
//  override def submitStringModification(stringModificationForNetwork: StringModificationForNetwork): StringModificationSubmissionResultForNetwork = {
//    val sReporter = new ServerReporter
//    val stringModification = stringModificationForNetwork.stringModification
//    val stringModID = stringModificationForNetwork.stringModID
//    val sourceCodeId = stringModificationForNetwork.sourceCodeId
//    sReporter.report(Info,
//      s"""Received a string modification from the client:
//         |  webElementID: ${stringModification.webElementID}
//         |  modified WebAttribute${stringModification.modifiedWebAttribute}
//         |  new value: ${stringModification.newValue}
//         |  id: ${stringModID}
//       """.stripMargin
//    )
//    val weID = stringModification.webElementID
//    val weExprFromSourceMap = Memory.getSourceMap(sourceCodeId) match {
//      case Some(sourceMap) =>
//        sourceMap.webElementIDToExpr(weID)
//      case None =>
//        throw new Exception(s"Could not find code with sourceCodeId = $sourceCodeId, maybe not up-to-date (last recorded is "+Memory.lastSourceId+")")
//    }
//
//    sReporter.report(Info,
//      s"""Here's what has been found in the sourceMap for the webElementID $weID:
//         |${weExprFromSourceMap}
//       """.stripMargin)
//
//    StringModificationSubmissionResultForNetwork(StringModificationProcessor.process(stringModification, sourceCodeId, sReporter), sourceCodeId, stringModID)
//  }
//}