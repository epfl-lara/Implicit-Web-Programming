package programEvaluator

import leonLibraryInShared.webDSL_Client.webDescription_Client.WebPage

/**
  * Created by dupriez on 3/10/16.
  */
case class ProgramEvaluationResult(webPageOpt: Option[WebPage], evaluationLog: String)
