package it.unibo.scafi.message.primitive

import java.nio.charset.StandardCharsets

import scala.scalanative.libc.stddef.size_t
import scala.scalanative.posix.inttypes.uint8_t
import scala.scalanative.unsafe.{ exported, CBool, CFuncPtr1, CFuncPtr2, CInt, CString, CVoidPtr, Ptr }
import scala.scalanative.unsafe.Size.intToSize
import scala.scalanative.unsigned.UInt
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.message.CBinaryCodable
import it.unibo.scafi.message.CBinaryCodable.*
import it.unibo.scafi.utils.CUtils.{ asVoidPtr, freshPointer, toByteArray, toUnconfinedCString, toUnconfinedUint8Array }

object PrimitiveCodables:

  @exported("codable_int")
  def codable(id: CInt): Ptr[CBinaryCodable] =
    val intPtr = freshPointer[CInt]
    !intPtr = id
    freshPointer[CBinaryCodable].tap: codable =>
      codable.data = intPtr.asVoidPtr
      codable.typeName = toUnconfinedCString("number")
      codable.encode = CFuncPtr2.fromScalaFunction(encodeInt)
      codable.decode = CFuncPtr2.fromScalaFunction(decodeInt)
      codable.equalsFn = CFuncPtr2.fromScalaFunction(eqInts)
      codable.hashFn = CFuncPtr1.fromScalaFunction(hashInt)
      codable.toStr = CFuncPtr1.fromScalaFunction(strInt)

  private def eqInts(a: CVoidPtr, b: CVoidPtr): CBool = (!asCIntPtr(a.data)) == (!asCIntPtr(b.data))

  private def hashInt(a: CVoidPtr): UInt = (!asCIntPtr(a.data)).hashCode().toUInt

  private def strInt(a: Ptr[CBinaryCodable]): CString = toUnconfinedCString((!asCIntPtr(a.data)).toString)

  private def encodeInt(data: CVoidPtr, size: Ptr[size_t]): Ptr[uint8_t] =
    val valueAsBytes = (!asCIntPtr(data)).toString.getBytes(StandardCharsets.UTF_8)
    val uint8Array = valueAsBytes.toUnconfinedUint8Array
    !size = valueAsBytes.length.toCSize
    uint8Array

  private def decodeInt(data: Ptr[uint8_t], size: size_t): Ptr[CBinaryCodable] =
    val value = new String(data.toByteArray(size), StandardCharsets.UTF_8).toInt
    codable(value)

  given asCBinaryCodablePtr: Conversion[CVoidPtr, Ptr[CBinaryCodable]] = _.asInstanceOf[Ptr[CBinaryCodable]]

  given asCIntPtr: Conversion[CVoidPtr, Ptr[CInt]] = _.asInstanceOf[Ptr[CInt]]
end PrimitiveCodables
