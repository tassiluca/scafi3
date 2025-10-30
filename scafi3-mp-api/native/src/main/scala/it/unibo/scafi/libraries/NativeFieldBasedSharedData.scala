package it.unibo.scafi.libraries

import scala.scalanative.unsafe.{ exported, fromCString, CString, Ptr, Zone }

import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.libraries.NativeFieldBasedSharedData.given
import it.unibo.scafi.message.CBinaryCodable.given_Hash_Ptr
import it.unibo.scafi.message.NativeBinaryCodable.nativeBinaryCodable
import it.unibo.scafi.nativebindings.aliases.NValues
import it.unibo.scafi.nativebindings.structs.{ BinaryCodable as CBinaryCodable, Field as CField }
import it.unibo.scafi.runtime.{ Allocator, NativeMemoryContext }
import it.unibo.scafi.types.{ CMap, EqWrapper, NativeTypes, PortableTypes }
import it.unibo.scafi.utils.CUtils.{ asVoidPtr, toUnconfinedCString }
import it.unibo.scafi.utils.libraries.Iso
import it.unibo.scafi.utils.libraries.Iso.given

/**
 * A custom portable definition of a field-based `SharedData` structure for native platform.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeFieldBasedSharedData extends PortableLibrary:
  self: PortableTypes & PortableExchangeCalculusLibrary & NativeTypes & NativeMemoryContext =>

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  override type SharedData[Value] = Ptr[CField]

  override given [Value](using Arena, Allocator): Iso[SharedData[Value], language.SharedData[Value]] = Iso(
    cFieldPtr =>
      val field = language.sharedDataApplicative.pure((!cFieldPtr).default_value.asInstanceOf[Value])
      val scalaNValues = CMap.of((!cFieldPtr).neighbor_values).toMap
      scalaNValues.foldLeft(field): (f, n) =>
        f.set(EqWrapper(n._1).asInstanceOf[language.DeviceId], n._2)
    ,
    scalaField =>
      NativeFieldBasedSharedData.of(
        scalaField.default.asInstanceOf[Ptr[CBinaryCodable]],
        scalaField.neighborValues.map((id, v) => (deviceIdConv(id), v)).toMap,
      ),
  )
end NativeFieldBasedSharedData

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object NativeFieldBasedSharedData:

  def of(default: Ptr[CBinaryCodable], neighborValues: Ptr[Byte])(using z: Zone, a: Allocator): Ptr[CField] =
    nativeBinaryCodable.register(default)
    a.track(default)
    CField(
      default_value = default,
      neighbor_values = neighborValues,
    )

  @exported("field_to_str")
  def fieldToString(sd: Ptr[CField]): CString =
    val defaultValuePtr = (!sd).default_value
    val defaultStr = fromCString((!defaultValuePtr).to_str(defaultValuePtr))
    val neighborsStr = CMap
      .of[Ptr[CBinaryCodable], Ptr[CBinaryCodable]]((!sd).neighbor_values)
      .toMap
      .map((nid, nv) => fromCString((!nid).to_str(nid)) + " -> " + fromCString((!nv).to_str(nv)))
      .toList
      .sorted
      .mkString("[", ", ", "]")
    s"Field($defaultStr, $neighborsStr)".toUnconfinedCString // memory needs to be freed by the caller

  /** Neighbor values are not exposed to C client as CMap, rather as NValues alias, a.k.a. an opaque pointer. */
  given Iso[Ptr[Byte], NValues] = Iso(_.asInstanceOf[NValues], _.asInstanceOf[Ptr[Byte]])
end NativeFieldBasedSharedData
