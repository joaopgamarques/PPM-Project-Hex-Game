package Project

// Implement the class MyRandom.
case class MyRandom(seed: Long) extends RandomWithState {
  override def nextInt(n: Int): (Int, MyRandom) = {
    val nextSeed: Long = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextRandom: MyRandom = MyRandom(nextSeed)
    val m: Int = ((nextSeed >>> 16).toInt) % n
    (math.abs(m), nextRandom)
  }
}