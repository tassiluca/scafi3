package it.unibo.scafi.types

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ CFuncPtr0, CFuncPtr1, CStruct1, CStruct2, CVoidPtr, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.libraries.PortableFieldBasedSharedData
import it.unibo.scafi.utils.CUtils.freshPointer

import libscafi3.all.BinaryCodable

object ExportedNativeTypes:
  type CNeighborhood = CMap

  type CSharedData = CStruct2[
    /* default_value */ Ptr[BinaryCodable],
    /* neighbor_values */ CNeighborhood,
  ]

  type CFieldBasedSharedData = CStruct1[
    /* of */ CFuncPtr1[Ptr[BinaryCodable], Ptr[CSharedData]],
  ]

  type CCommonLibrary = CStruct2[
    /* local_id */ CFuncPtr0[CVoidPtr],
    /* device_id */ CFuncPtr0[Ptr[CSharedData]],
  ]

  type CAggregateLibrary = CStruct2[
    /* Field */ Ptr[CFieldBasedSharedData],
    /* common */ Ptr[CCommonLibrary],
  ]
end ExportedNativeTypes

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeTypesConversions:
  self: PortableFieldBasedSharedData & NativeTypes =>
  export ExportedNativeTypes.*

  given Iso[Ptr[CSharedData], Field[Ptr[BinaryCodable]]] =
    Iso[Ptr[CSharedData], Field[Ptr[BinaryCodable]]](csd =>
      Field((!csd)._1, (!csd)._2.asInstanceOf[Map[language.DeviceId, Ptr[BinaryCodable]]]),
    )(sd =>
      freshPointer[CSharedData].tap: nativeSharedData =>
        nativeSharedData._1 = sd.default
        nativeSharedData._2 = CMap.empty,
    )
end NativeTypesConversions
