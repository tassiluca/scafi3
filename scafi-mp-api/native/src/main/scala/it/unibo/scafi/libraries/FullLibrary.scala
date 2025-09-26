package it.unibo.scafi.libraries

import java.util.concurrent.atomic.AtomicReference

import scala.scalanative.unsafe.{ CFuncPtr0, CFuncPtr1, CStruct1, CStruct2, CStruct3, CVoidPtr, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.libraries.FullLibrary.libraryRef
import it.unibo.scafi.types.{ CMap, NativeTypes }
import it.unibo.scafi.utils.CUtils.freshPointer

import libscafi3.all.BinaryCodable

class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData,
) extends FullPortableLibrary
    with NativeFieldBasedSharedData
    with NativeTypes:

  override given valueCodable[Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
    override def encode(value: Value): Format = ???
    override def decode(data: Format): Value = ???
    override def register(value: Value): Unit = ???

  type CReturnSending = ReturnSending[CVoidPtr]

  type CFieldBasedSharedData = CStruct1[
    /* of */ Function1[Ptr[BinaryCodable], Ptr[CSharedData]],
  ]

  type CCommonLibrary = CStruct2[
    /* local_id */ Function0[Ptr[BinaryCodable]],
    /* device_id */ Function0[Ptr[CSharedData]],
  ]

  type CBranchingLibrary = CStruct1[
    /* branch */ Function3[Boolean, Function0[CVoidPtr], Function0[CVoidPtr], CVoidPtr],
  ]

  type CExchangeLibrary = CStruct1[
    /* exchange */ Function2[Ptr[CSharedData], Function1[Ptr[CSharedData], CReturnSending], Ptr[CSharedData]],
  ]

  type CAggregateLibrary = CStruct3[
    /* Field */ CFieldBasedSharedData,
    /* common */ CCommonLibrary,
    /* branching */ CBranchingLibrary,
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
    cAggregateLibrary._2._2 = () => libraryRef.get().device
    cAggregateLibrary._3._1 = (condition: Boolean, trueBranch: Function0[CVoidPtr], falseBranch: Function0[CVoidPtr]) =>
      libraryRef.get().branch_(condition)(trueBranch)(falseBranch)
    cAggregateLibrary
  end asNative
end FullLibrary

object FullLibrary:

  /**
   * Singleton reference to the full library instance. Unfortunately, due to limitations in C, it is not possible to
   * close a function pointer over local state paramaters, so we need to store the instance in a global variable.
   *
   * This is not ideal, but does not cause issues in practice since rounds are executed sequentially and independently.
   */
  private[FullLibrary] val libraryRef = new AtomicReference[FullLibrary]()
