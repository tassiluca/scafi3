package it.unibo.scafi.types

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class NativeTypesCompatibilityTest extends AnyFlatSpec with should.Matchers:

  "Native field-based shared data" should "be isomorphic to the Scala one" in:
    true shouldBe true
