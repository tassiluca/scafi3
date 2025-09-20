package it.unibo.scafi.types

import scala.scalanative.unsafe.{ exported, CVoidPtr }

/**
 * A generic, C-interoperable tuple of two elements. This tuple is heterogeneous: the two elements can be of different
 * types. Internally, it only stores raw `void*` pointers to the actual values, so callers are responsible for ensuring
 * that the pointers are of coherent types. Dereferencing or misinterpreting these pointers can result in undefined or
 * unsafe behavior.
 * @note
 *   Unlike Scala's built-in tuples, this container does not enforce type safety at compile time and is intended
 *   primarily as a low-level, flexible utility for C/C++ interoperability.
 * @param underlying
 *   the underlying Scala Native tuple implementation delegating the operations to
 * @note
 *   companion object for exported functions callable from C/C++
 */
class CTuple(underlying: (CVoidPtr, CVoidPtr)) extends Product2[CVoidPtr, CVoidPtr]:
  export underlying.{ _1, _2, canEqual }

object CTuple:

  @exported
  def pair(x: CVoidPtr, y: CVoidPtr): CTuple = CTuple((x, y))

  @exported
  def fst(t: CTuple): CVoidPtr = t._1

  @exported
  def snd(t: CTuple): CVoidPtr = t._2
