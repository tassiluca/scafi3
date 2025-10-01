package it.unibo.scafi.libraries

import scala.scalanative.posix.string.strdup
import scala.scalanative.unsafe.{ exported, fromCString, toCString, CFuncPtr2, CString, CStruct2, CVoidPtr, Ptr, Zone }
import scala.scalanative.unsigned.UInt
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.types.{ CBinaryCodable, CMap, EqPtr }
import it.unibo.scafi.types.CBinaryCodable.toStr
import it.unibo.scafi.utils.CUtils.freshPointer

/**
 * A custom portable definition of a field-based `SharedData` structure for native platform.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeFieldBasedSharedData extends PortableLibrary:
  self: PortableTypes & PortableExchangeCalculusLibrary =>

  export NativeFieldBasedSharedData.CSharedData

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  override type SharedData[Value] = Ptr[CSharedData]

  override given [Value]: Iso[SharedData[Value], language.SharedData[Value]] =
    Iso[SharedData[Value], language.SharedData[Value]](cField =>
      val field = language.sharedDataApplicative.pure((!cField)._1.asInstanceOf[Value])
      (!cField)._2.toScalaMap.foldLeft(field): (f, n) =>
        f.set(n._1.asInstanceOf[language.DeviceId], n._2.asInstanceOf[Value]),
    )(f =>
      freshPointer[CSharedData].tap:
        cField => // TODO: when to free this memory?
          cField._1 = f.default.asInstanceOf[Ptr[CBinaryCodable]]
          cField._2 = CMap(
            collection.mutable.Map
              .from(f.neighborValues.asInstanceOf[collection.immutable.Map[EqPtr, CVoidPtr]])
              .map(_.ptr -> _),
            if f.neighborValues.isEmpty then (_: CVoidPtr, _: CVoidPtr) => false
            else f.neighborValues.head._1.asInstanceOf[EqPtr].equals,
            if f.neighborValues.isEmpty then (_: CVoidPtr) => UInt.valueOf(0)
            else f.neighborValues.head._1.asInstanceOf[EqPtr].hash,
          ),
    )

end NativeFieldBasedSharedData

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object NativeFieldBasedSharedData:

  type CSharedData = CStruct2[ /* default_value */ Ptr[CBinaryCodable], /* neighbor_values */ CMap]

  extension (sd: Ptr[CSharedData])
    def default: Ptr[CBinaryCodable] = (!sd)._1
    def neighborValues: CMap = (!sd)._2

  @exported("shared_data_to_string")
  def sharedDataToString(sd: Ptr[CSharedData]): CString =
    val defaultStr = fromCString(sd.default.toStr(sd.default))
    val neighborsStr = sd.neighborValues.toScalaMap
      .map: (nid, nv) =>
        nid.asInstanceOf[EqPtr].ptr.asInstanceOf[Ptr[CBinaryCodable]] -> nv.asInstanceOf[Ptr[CBinaryCodable]]
      .map: (nid, nv) =>
        fromCString(nid.toStr(nid)) + " -> " + fromCString(nv.toStr(nv))
      .toList
      .sorted
      .mkString("[", ", ", "]")
    Zone(strdup(toCString(s"Field($defaultStr, $neighborsStr)"))) // memory needs to be freed by the caller
end NativeFieldBasedSharedData
