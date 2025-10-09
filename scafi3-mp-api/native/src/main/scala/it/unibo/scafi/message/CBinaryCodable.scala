package it.unibo.scafi.message

import scala.scalanative.libc.stddef.size_t
import scala.scalanative.posix.inttypes.uint8_t
import scala.scalanative.unsafe.{ CBool, CFuncPtr1, CFuncPtr2, CString, CStruct7, CVoidPtr, Ptr }
import scala.scalanative.unsigned.UInt

import cats.kernel.Hash

/**
 * A C function pointer that determines equality between two C void pointers.
 */
type CEquals = CFuncPtr2[CVoidPtr, CVoidPtr, CBool]

/**
 * A C function pointer that computes a hash code for a C void pointer.
 */
type CHash = CFuncPtr1[CVoidPtr, UInt]

type CBinaryCodable = CStruct7[
  /* data */ CVoidPtr,
  /* type_name */ CString,
  /* encode */ CFuncPtr2[CVoidPtr, Ptr[size_t], Ptr[uint8_t]],
  /* decode */ CFuncPtr2[Ptr[uint8_t], size_t, CVoidPtr],
  /* equals */ CEquals,
  /* hash */ CHash,
  /* to_str */ CFuncPtr1[CVoidPtr, CString],
]

object CBinaryCodable:

  extension (ptr: Ptr[CBinaryCodable])
    def data: CVoidPtr = (!ptr)._1
    def data_=(d: CVoidPtr): Unit = (!ptr)._1 = d
    def typeName: CString = (!ptr)._2
    def typeName_=(name: CString): Unit = (!ptr)._2 = name
    def encode: CFuncPtr2[CVoidPtr, Ptr[size_t], Ptr[uint8_t]] = (!ptr)._3
    def encode_=(e: CFuncPtr2[CVoidPtr, Ptr[size_t], Ptr[uint8_t]]): Unit = (!ptr)._3 = e
    def decode: CFuncPtr2[Ptr[uint8_t], size_t, CVoidPtr] = (!ptr)._4
    def decode_=(d: CFuncPtr2[Ptr[uint8_t], size_t, CVoidPtr]): Unit = (!ptr)._4 = d
    def equalsFn: CEquals = (!ptr)._5
    def equalsFn_=(e: CEquals): Unit = (!ptr)._5 = e
    def hashFn: CHash = (!ptr)._6
    def hashFn_=(h: CHash): Unit = (!ptr)._6 = h
    def toStr: CFuncPtr1[CVoidPtr, CString] = (!ptr)._7
    def toStr_=(t: CFuncPtr1[CVoidPtr, CString]): Unit = (!ptr)._7 = t

  /** An instance of [[Hash]] for [[it.unibo.scafi.message.CBinaryCodable]]. */
  given Hash[Ptr[CBinaryCodable]] = new Hash[Ptr[CBinaryCodable]]:
    override def hash(x: Ptr[CBinaryCodable]): Int = x.hashFn(x).toInt
    override def eqv(x: Ptr[CBinaryCodable], y: Ptr[CBinaryCodable]): Boolean = x.equalsFn(x, y)
end CBinaryCodable
