package memory

import programEvaluator.SourceMap

/**
  * Created by dupriez on 2/25/16.
  */
object Memory {
  private var _sourceMap : SourceMap = null
  def sourceMap_=(s: SourceMap) = {
    _sourceMap = s
  }
  def sourceMap : SourceMap = {
    if (_sourceMap == null) {
      throw new RuntimeException("Memory was asked for the sourceMap, while the sourceMap var contained a null")
    }
    else {
      _sourceMap
    }
  }
}
