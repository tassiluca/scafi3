package it.unibo.scafi.types

import scala.scalanative.unsafe.{ CFuncPtr0, CFuncPtr1, CStruct1, CStruct2, Ptr }
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
    /* local_id */ CFuncPtr0[Ptr[BinaryCodable]],
    /* device_id */ CFuncPtr0[Ptr[BinaryCodable]],
  ]

  type CAggregateLibrary = CStruct2[
    /* Field */ CFieldBasedSharedData,
    CCommonLibrary,
  ]
end ExportedNativeTypes

trait NativeTypesConversions:
  self: PortableFieldBasedSharedData & NativeTypes =>
  export ExportedNativeTypes.*

  given Iso[CSharedData, Field[Ptr[BinaryCodable]]] =
    Iso[CSharedData, Field[Ptr[BinaryCodable]]](csd =>
      Field(csd._1, csd._2.asInstanceOf[Map[language.DeviceId, Ptr[BinaryCodable]]]),
    )(sd =>
      freshPointer[CSharedData]().tap: nativeSharedData =>
        nativeSharedData._1 = sd.default
        nativeSharedData._2 = sd.neighborValues,
    )

  def nativeAggregateLibrary(): Ptr[CAggregateLibrary] =
    freshPointer[CAggregateLibrary]().tap: lib =>
      lib._1 = freshPointer[CFieldBasedSharedData]().tap: fbsd =>
        fbsd._1 = (local: Ptr[BinaryCodable]) =>
          freshPointer[CSharedData]().tap: sd =>
            sd._1 = local
            sd._2 = CMap.empty
      lib._2 = freshPointer[CCommonLibrary]().tap: cl =>
        cl._1 = () => ???
        cl._2 = () => ???
end NativeTypesConversions
