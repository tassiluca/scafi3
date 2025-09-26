package it.unibo.scafi.presentation

import scala.scalanative.unsafe.Ptr

import it.unibo.scafi.message.UniversalCodable

import libscafi3.structs.BinaryCodable

object NativeBinaryCodable:

  given nativeBinaryCodable: UniversalCodable[Ptr[BinaryCodable], Array[Byte]] =
    new UniversalCodable[Ptr[BinaryCodable], Array[Byte]]:
      override def encode(value: Ptr[BinaryCodable]): Array[Byte] = ???
      override def decode(data: Array[Byte]): Ptr[BinaryCodable] = ???
      override def register(value: Ptr[BinaryCodable]): Unit = ???
