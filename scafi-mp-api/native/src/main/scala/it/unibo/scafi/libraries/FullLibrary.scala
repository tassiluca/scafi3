package it.unibo.scafi.libraries

import java.util.concurrent.atomic.AtomicReference

import scala.scalanative.unsafe.{ CFuncPtr0, CFuncPtr1, CStruct1, CStruct2, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.libraries.FullLibrary.libraryRef
import it.unibo.scafi.types.{ CMap, NativeTypes }
import it.unibo.scafi.utils.CUtils.freshPointer

import libscafi3.all.BinaryCodable

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData,
) extends FullPortableLibrary
    with NativeFieldBasedSharedData
    with NativeTypes:

  override given valueCodable[Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
    override def encode(value: Value): Format = ???
    override def decode(data: Format): Value = ???
    override def register(value: Value): Unit = ???

  type CFieldBasedSharedData = CStruct1[
    /* of */ CFuncPtr1[Ptr[BinaryCodable], Ptr[CSharedData]],
  ]

  type CCommonLibrary = CStruct2[
    /* local_id */ CFuncPtr0[Ptr[BinaryCodable]],
    /* device_id */ CFuncPtr0[Ptr[CSharedData]],
  ]

  type CAggregateLibrary = CStruct2[
    /* Field */ CFieldBasedSharedData,
    /* common */ CCommonLibrary,
  ]

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def asNative: Ptr[CAggregateLibrary] =
    // TODO: handle freeing of the allocated memory
    libraryRef.set(this)
    val cAggregateLibrary: Ptr[CAggregateLibrary] = freshPointer[CAggregateLibrary]
    cAggregateLibrary._1._1 = (local: Ptr[BinaryCodable]) =>
      freshPointer[CSharedData].tap: sd =>
        sd._1 = local
        sd._2 = CMap.empty
    cAggregateLibrary._2._1 = () => libraryRef.get().localId.asInstanceOf[Ptr[BinaryCodable]]
    // val deviceId = CFuncPtr0.fromScalaFunction[Ptr[CSharedData]]: () =>
    //   println(s"[NativeTypesConversions] Asked for device id")
    //   ???
    // cAggregateLibrary._2._2 = deviceId
    cAggregateLibrary
end FullLibrary

object FullLibrary:

  /**
   * Singleton reference to the full library instance. Unfortunately, due to limitations in C, it is not possible to
   * close a function pointer over local state paramaters, so we need to store the instance in a global variable.
   *
   * This is not ideal, but does not cause issues in practice since rounds are executed sequentially and independently.
   */
  private[FullLibrary] val libraryRef = new AtomicReference[FullLibrary]()
