package it.unibo.scafi.utils

object SimpleGrids:

  def vonNeumannGrid[Result](rows: Int, cols: Int)(f: (Int, Seq[Int]) => Result): Seq[Result] =
    val areConnected = (a: Int, b: Int) =>
      val (ax, ay) = (a / cols, a % cols)
      val (bx, by) = (b / cols, b % cols)
      (ax == bx && math.abs(ay - by) == 1) || (ay == by && math.abs(ax - bx) == 1)
    for
      i <- 0 until rows * cols
      neighbors = (0 until rows * cols).filter(j => areConnected(i, j))
    yield f(i, neighbors)
