package it.unibo.scafi.types

import scala.compiletime.uninitialized
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ CFuncPtr0, CFuncPtr1, CStruct1, CStruct2, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.libraries.{ FullLibrary, PortableFieldBasedSharedData }
import it.unibo.scafi.utils.CUtils.freshPointer

import libscafi3.all.BinaryCodable
import scala.scalanative.unsafe.CVoidPtr

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

object LibraryRef:
  import ExportedNativeTypes.*

  var fullLibrary: FullLibrary = uninitialized

  def nativeAggregateLibrary(): Ptr[CAggregateLibrary] =
    val cAggregateLibrary: Ptr[CAggregateLibrary] = freshPointer[CAggregateLibrary]()
    (!cAggregateLibrary)._1 = freshPointer[CFieldBasedSharedData]()
    (!cAggregateLibrary)._2 = freshPointer[CCommonLibrary]()

    val fieldOfFn = CFuncPtr1.fromScalaFunction: (local: Ptr[BinaryCodable]) =>
      freshPointer[CSharedData]().tap: sd =>
        sd._1 = local
        sd._2 = CMap.empty
    (!(!cAggregateLibrary)._1)._1 = fieldOfFn

    val localIdFn = CFuncPtr0.fromScalaFunction(() => fullLibrary.localId.asInstanceOf[CVoidPtr])
    (!(!cAggregateLibrary)._2)._1 = localIdFn

    // val deviceId = CFuncPtr0.fromScalaFunction[Ptr[CSharedData]]: () =>
    //   println(s"[NativeTypesConversions] Asked for device id")
    //   ???
    // (!(!cAggregateLibrary)._2)._2 = deviceId

    cAggregateLibrary
  end nativeAggregateLibrary
end LibraryRef

trait NativeTypesConversions:
  self: PortableFieldBasedSharedData & NativeTypes =>
  export ExportedNativeTypes.*

  given Iso[Ptr[CSharedData], Field[Ptr[BinaryCodable]]] =
    Iso[Ptr[CSharedData], Field[Ptr[BinaryCodable]]](csd =>
      Field((!csd)._1, (!csd)._2.asInstanceOf[Map[language.DeviceId, Ptr[BinaryCodable]]]),
    )(sd =>
      freshPointer[CSharedData]().tap: nativeSharedData =>
        nativeSharedData._1 = sd.default
        nativeSharedData._2 = CMap.empty,
    )
end NativeTypesConversions
