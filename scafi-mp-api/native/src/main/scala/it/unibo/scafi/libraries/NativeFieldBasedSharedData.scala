package it.unibo.scafi.libraries

import cats.kernel.Hash

import scala.scalanative.posix.string.strdup
import scala.scalanative.unsafe.{ exported, fromCString, toCString, CString, CStruct2, Ptr, Zone }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.message.CBinaryCodable
import it.unibo.scafi.message.CBinaryCodable.toStr
import it.unibo.scafi.message.NativeBinaryCodable.nativeBinaryCodable
import it.unibo.scafi.types.{ CMap, NativeTypes, PortableTypes }
import it.unibo.scafi.utils.CUtils.freshPointer
import it.unibo.scafi.types.EqWrapper

import NativeFieldBasedSharedData.neighborValues

/**
 * A custom portable definition of a field-based `SharedData` structure for native platform.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeFieldBasedSharedData extends PortableLibrary:
  self: PortableTypes & PortableExchangeCalculusLibrary & NativeTypes =>

  export NativeFieldBasedSharedData.CField

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  override type SharedData[Value] = Ptr[CField]

  given Hash[Ptr[CBinaryCodable]] = new Hash[Ptr[CBinaryCodable]]:
    override def hash(x: Ptr[CBinaryCodable]): Int = ???
    override def eqv(x: Ptr[CBinaryCodable], y: Ptr[CBinaryCodable]): Boolean = ???

  override given [Value] => Iso[SharedData[Value], language.SharedData[Value]] =
    Iso[SharedData[Value], language.SharedData[Value]](cField =>
      val field = language.sharedDataApplicative.pure((!cField)._1.asInstanceOf[Value])
      cField.neighborValues.toScalaMap.foldLeft(field): (f, n) =>
        f.set(EqWrapper(n._1).asInstanceOf[language.DeviceId], n._2.asInstanceOf[Value]),
    )(scalaField =>
      val nvalues: Map[language.DeviceId, Value] =
        scalaField.neighborValues.map((id, v) => (deviceIdConv(id), v)).toMap
      NativeFieldBasedSharedData.of(
        scalaField.default.asInstanceOf[Ptr[CBinaryCodable]],
        nvalues.asInstanceOf[Map[Ptr[CBinaryCodable], Ptr[CBinaryCodable]]],
      ),
    )
end NativeFieldBasedSharedData

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object NativeFieldBasedSharedData:

  type CField = CStruct2[
    /* default_value */ Ptr[CBinaryCodable],
    /* neighbor_values */ CMap[Ptr[CBinaryCodable], Ptr[CBinaryCodable]],
  ]

  def of(default: Ptr[CBinaryCodable], neighborValues: CMap[Ptr[CBinaryCodable], Ptr[CBinaryCodable]]): Ptr[CField] =
    nativeBinaryCodable.register(default)
    freshPointer[CField].tap: cField =>
      cField.default = default
      cField.neighborValues = neighborValues

  extension (sd: Ptr[CField])
    def default: Ptr[CBinaryCodable] = (!sd)._1
    def default_=(v: Ptr[CBinaryCodable]): Unit = (!sd)._1 = v
    def neighborValues: CMap[Ptr[CBinaryCodable], Ptr[CBinaryCodable]] = (!sd)._2
    def neighborValues_=(m: CMap[Ptr[CBinaryCodable], Ptr[CBinaryCodable]]): Unit = (!sd)._2 = m

  @exported("field_to_str")
  def fieldToString(sd: Ptr[CField]): CString =
    val defaultStr = fromCString(sd.default.toStr(sd.default))
    val neighborsStr = sd.neighborValues.toScalaMap
      .map: (nid, nv) =>
        fromCString(nid.toStr(nid)) + " -> " + fromCString(nv.toStr(nv))
      .toList
      .sorted
      .mkString("[", ", ", "]")
    Zone(strdup(toCString(s"Field($defaultStr, $neighborsStr)"))) // memory needs to be freed by the caller
end NativeFieldBasedSharedData
