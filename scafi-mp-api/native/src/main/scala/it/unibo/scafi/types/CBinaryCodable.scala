package it.unibo.scafi.types

import scala.scalanative.libc.stddef.size_t
import scala.scalanative.posix.inttypes.uint8_t
import scala.scalanative.unsafe.{ CFuncPtr2, CString, CStruct5, CVoidPtr, Ptr }

type CBinaryCodable = CStruct5[
  /* data */ CVoidPtr,
  /* type_name */ CString,
  /* encode */ CFuncPtr2[CVoidPtr, Ptr[size_t], Ptr[uint8_t]],
  /* decode */ CFuncPtr2[Ptr[uint8_t], size_t, CVoidPtr],
  /* equals */ CFuncPtr2[CVoidPtr, CVoidPtr, Boolean],
]

object CBinaryCodable:

  extension (ptr: Ptr[CBinaryCodable])
    def data: CVoidPtr = (!ptr)._1
    def typeName: CString = (!ptr)._2
    def encode: CFuncPtr2[CVoidPtr, Ptr[size_t], Ptr[uint8_t]] = (!ptr)._3
    def decode: CFuncPtr2[Ptr[uint8_t], size_t, CVoidPtr] = (!ptr)._4
    def equalsFn: CEquals = (!ptr)._5
