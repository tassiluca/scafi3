package it.unibo.scafi.types

import scala.scalanative.libc.stddef.size_t
import scala.scalanative.posix.inttypes.uint8_t
import scala.scalanative.unsafe.{ CFuncPtr1, CFuncPtr2, CString, CStruct6, CVoidPtr, Ptr }

type CBinaryCodable = CStruct6[
  /* data */ CVoidPtr,
  /* type_name */ CString,
  /* encode */ CFuncPtr2[CVoidPtr, Ptr[size_t], Ptr[uint8_t]],
  /* decode */ CFuncPtr2[Ptr[uint8_t], size_t, CVoidPtr],
  /* equals */ CFuncPtr2[CVoidPtr, CVoidPtr, Boolean],
  /* to_str */ CFuncPtr1[CVoidPtr, CString],
]

object CBinaryCodable:

  extension (ptr: Ptr[CBinaryCodable])
    def data: CVoidPtr = (!ptr)._1
    def typeName: CString = (!ptr)._2
    def encode: CFuncPtr2[CVoidPtr, Ptr[size_t], Ptr[uint8_t]] = (!ptr)._3
    def decode: CFuncPtr2[Ptr[uint8_t], size_t, CVoidPtr] = (!ptr)._4
    def equalsFn: CEquals = (!ptr)._5
    def toStr: CFuncPtr1[CVoidPtr, CString] = (!ptr)._6
