package programEvaluator

/**
  * Created by dupriez on 3/10/16.
  */

class IDGenerator {
  private var _counter = 0
  def generateID() = {
    _counter = _counter+1
    _counter
  }
  def reset() = {
    _counter = 0
  }
}