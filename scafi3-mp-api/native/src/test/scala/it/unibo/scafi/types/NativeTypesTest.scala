package it.unibo.scafi.nativebindings.it.unibo.scafi.types

import scala.scalanative.unsafe.{ alloc, Ptr, Zone }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.types.NativeTypes

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class NativeTypesTest extends AnyFlatSpec with should.Matchers with NativeTypes:

  "Seq Iso" should "correctly convert between CArray and Scala Seq" in:
    Zone:
      val scalaSeq = (1 to 10).map(x => alloc[Int]().tap(ptr => !ptr = x)).toSeq
      val cArray: Seq[Ptr[Int]] = scalaSeq
      val backToScala: collection.Seq[Ptr[Int]] = cArray
      backToScala.map(!_) shouldEqual (1 to 10)
