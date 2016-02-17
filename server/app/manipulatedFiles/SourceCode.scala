package manipulatedFiles

object Sort {
  def sort2(x : BigInt, y : BigInt): (BigInt, BigInt) =  {
    if (x < y) {
      (x, y)
    } else if (x == y) {
      (x, x)
    } else {
      (y, x)
    }
  }
}
