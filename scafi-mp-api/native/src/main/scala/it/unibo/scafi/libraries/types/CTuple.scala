package it.unibo.scafi.libraries.types

import scala.scalanative.unsafe.exported

class CTuple[A, B](underlying: (A, B)) extends Product2[A, B]:
  export underlying.{ _1, _2, canEqual }

object CTuple:

  @exported
  def pair[A, B](x: A, y: B): CTuple[A, B] = CTuple((x, y))

  @exported
  def fst[A, B](t: CTuple[A, B]): A = t._1

  @exported
  def snd[A, B](t: CTuple[A, B]): B = t._2
