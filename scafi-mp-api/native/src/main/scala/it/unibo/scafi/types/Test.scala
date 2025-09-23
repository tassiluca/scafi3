package it.unibo.scafi.types

import scala.compiletime.uninitialized
import scala.scalanative.unsafe.*

class Zoo(val x: Int, val y: String)

object Foo:
  var value: Zoo = uninitialized

  def foo(): CString =
    def cFunc: CFuncPtr0[CString] = () => s"${value.x} -- ${value.y}".toCString
    cFunc()

class Bar:

  def bar(): CString =
    Foo.value = Zoo(9, "hello sfiggy")
    Foo.foo()

object Test:

  @exported
  def test(): CString =
    val b = new Bar()
    b.bar()

import it.unibo.scafi.utils.CUtils.freshPointer
import scala.util.chaining.scalaUtilChainingOps

extension (s: String)

  /**
   * @return
   *   a [[CString]] allocated in the heap, null-terminated, and containing the string `s`
   * @note
   *   The caller is responsible for freeing the memory allocated for the string.
   */
  def toCString: CString = freshPointer[CChar](s.length() + 1)
    .tap: ptr =>
      for i <- 0 until s.length() do ptr(i) = s.charAt(i).toByte
      ptr(s.length()) = 0.toByte
