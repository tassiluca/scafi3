package it.unibo.scafi.libraries

import java.util.concurrent.atomic.AtomicReference

import scala.scalanative.unsafe.{ Ptr, Zone }

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.language.fc.syntax.FieldCalculusSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.libraries.FullLibrary.{ allocatorRef, libraryRef }
import it.unibo.scafi.message.NativeBinaryCodable.nativeBinaryCodable
import it.unibo.scafi.nativebindings.structs.{
  AggregateLibrary as CAggregateLibrary,
  BinaryCodable as CBinaryCodable,
  Field as CField,
  FieldBasedSharedData as CFieldBasedSharedData,
  ReturnSending as CReturnSending,
}
import it.unibo.scafi.runtime.NativeMemoryContext
import it.unibo.scafi.types.{ CMap, EqWrapper, NativeTypes }

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData & FieldCalculusSyntax,
) extends FullPortableLibrary
    with NativeFieldBasedSharedData
    with NativeTypes
    with NativeMemoryContext:

  override given valueCodable[Value, Format]: UniversalCodable[Value, Format] =
    nativeBinaryCodable.asInstanceOf[UniversalCodable[Value, Format]]

  override given deviceIdConv[ID]: Conversion[language.DeviceId, ID] =
    _.asInstanceOf[EqWrapper[Ptr[CBinaryCodable]]].value.asInstanceOf[ID]

  override type ReturnSending = Ptr[CReturnSending]

  override given [Value](using Arena, Allocator): Conversion[ReturnSending, RetSend[language.SharedData[Value]]] = rs =>
    RetSend((!rs).returning, (!rs).sending)

  def asNative(using Zone, Allocator): Ptr[CAggregateLibrary] =
    libraryRef.set(this)
    allocatorRef.set((summon[Zone], summon[Allocator]))
    val lib = CAggregateLibrary()
    (!lib).Field = !CFieldBasedSharedData: (default: Ptr[CBinaryCodable]) =>
      val (zone, allocator) = allocatorRef.get()
      NativeFieldBasedSharedData.of(default, CMap.empty)(using zone, allocator)
    (!lib).local_id = () => libraryRef.get().localId.asInstanceOf[Ptr[CBinaryCodable]]
    (!lib).branch = (condition: Boolean, trueBranch: Function0[Ptr[Byte]], falseBranch: Function0[Ptr[Byte]]) =>
      libraryRef.get().branch_(condition)(trueBranch)(falseBranch)
    (!lib).exchange = (initial: Ptr[CField], f: Function1[Ptr[CField], ReturnSending]) =>
      val (zone, allocator) = allocatorRef.get()
      libraryRef.get().exchange_(initial)(f)(using zone, allocator)
    (!lib).share = (initial: Ptr[CBinaryCodable], f: Function1[Ptr[CField], Ptr[CBinaryCodable]]) =>
      val (zone, allocator) = allocatorRef.get()
      libraryRef.get().share_(initial)(f)(using zone, allocator)
    lib
end FullLibrary

object FullLibrary:
  import it.unibo.scafi.runtime.Allocator

  /**
   * Singleton reference to the full library instance. Unfortunately, due to limitations in C, it is not possible to
   * close a function pointer over local state parameters, so we need to store the instance in a global variable.
   *
   * This is not ideal, but does not cause issues in practice since rounds are executed sequentially and independently.
   */
  private[FullLibrary] val libraryRef = new AtomicReference[FullLibrary]()
  private[FullLibrary] val allocatorRef = new AtomicReference[(Zone, Allocator)]()
