package it.unibo.scafi.libraries

import java.util.concurrent.atomic.AtomicReference

import scala.scalanative.unsafe.{ exported, CFuncPtr0, CFuncPtr2, CStruct1, CStruct2, CStruct4, CVoidPtr, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.libraries.FullLibrary.libraryRef
import it.unibo.scafi.presentation.NativeBinaryCodable.nativeBinaryCodable
import it.unibo.scafi.types.{ CBinaryCodable, CMap, EqPtr, NativeTypes }
import it.unibo.scafi.types.CBinaryCodable.equalsFn
import it.unibo.scafi.utils.CUtils.freshPointer

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData,
) extends FullPortableLibrary
    with NativeFieldBasedSharedData
    with NativeTypes:

  override given valueCodable[Value, Format]: UniversalCodable[Value, Format] =
    nativeBinaryCodable.asInstanceOf[UniversalCodable[Value, Format]]

  type CReturnSending[T] = ReturnSending[T]

  type CFieldBasedSharedData = CStruct1[
    /* of */ Function1[Ptr[CBinaryCodable], Ptr[CSharedData]],
  ]

  type CCommonLibrary = CStruct2[
    /* local_id */ Function0[Ptr[CBinaryCodable]],
    /* device_id */ Function0[Ptr[CSharedData]],
  ]

  type CBranchingLibrary = CStruct1[
    /* branch */ Function3[Boolean, Function0[CVoidPtr], Function0[CVoidPtr], CVoidPtr],
  ]

  type CExchangeLibrary = CStruct1[
    /* exchange */ Function2[Ptr[CSharedData], Function1[Ptr[CSharedData], CReturnSending[Ptr[CSharedData]]], Ptr[
      CSharedData,
    ]],
  ]

  type CAggregateLibrary = CStruct4[
    /* Field */ CFieldBasedSharedData,
    /* common */ CCommonLibrary,
    /* branching */ CBranchingLibrary,
    /* exchange */ CExchangeLibrary,
  ]

  def asNative: Ptr[CAggregateLibrary] =
    libraryRef.set(this)
    val cAggregateLibrary: Ptr[CAggregateLibrary] = freshPointer[CAggregateLibrary] // TODO: free memory
    cAggregateLibrary._1._1 = (local: Ptr[CBinaryCodable]) =>
      nativeBinaryCodable.register(local)
      freshPointer[CSharedData].tap: sd => // TODO: memory leak here
        sd._1 = local
        sd._2 = CMap(
          collection.mutable.Map.empty,
          local.equalsFn.asInstanceOf[CFuncPtr2[CVoidPtr, CVoidPtr, Boolean]],
        )
    cAggregateLibrary._2._1 = () => libraryRef.get().localId.asInstanceOf[EqPtr].ptr.asInstanceOf[Ptr[CBinaryCodable]]
    cAggregateLibrary._2._2 = () => libraryRef.get().device
    cAggregateLibrary._3._1 = (condition: Boolean, trueBranch: Function0[CVoidPtr], falseBranch: Function0[CVoidPtr]) =>
      libraryRef.get().branch_(condition)(trueBranch)(falseBranch)
    cAggregateLibrary._4._1 =
      (initial: Ptr[CSharedData], f: Function1[Ptr[CSharedData], CReturnSending[Ptr[CSharedData]]]) =>
        libraryRef.get().exchange_(initial)(f)
    cAggregateLibrary
  end asNative
end FullLibrary

object FullLibrary:

  /**
   * Singleton reference to the full library instance. Unfortunately, due to limitations in C, it is not possible to
   * close a function pointer over local state parameters, so we need to store the instance in a global variable.
   *
   * This is not ideal, but does not cause issues in practice since rounds are executed sequentially and independently.
   */
  private[FullLibrary] val libraryRef = new AtomicReference[FullLibrary]()

  @exported("retsend")
  def returnSending(value: CVoidPtr): ReturnSending[CVoidPtr] = ReturnSending(value, value)

  @exported("return_sending")
  def returnSending(returning: CVoidPtr, send: CVoidPtr): ReturnSending[CVoidPtr] = ReturnSending(returning, send)
