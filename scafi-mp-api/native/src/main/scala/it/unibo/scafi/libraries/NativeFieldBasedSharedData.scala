package it.unibo.scafi.libraries

import scala.scalanative.posix.string.strdup
import scala.scalanative.unsafe.{ exported, fromCString, toCString, CFuncPtr2, CString, CStruct2, CVoidPtr, Ptr, Zone }
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

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  type CSharedData = CStruct2[
    /* default_value */ Ptr[CBinaryCodable],
    /* neighbor_values */ CMap,
  ]

  override type SharedData[Value] = Ptr[CSharedData]

  override given [Value]: Iso[SharedData[Value], language.SharedData[Value]] =
    Iso[SharedData[Value], language.SharedData[Value]](cField =>
      val field = language.sharedDataApplicative.pure((!cField)._1.asInstanceOf[Value])
      (!cField)._2.toScalaMap.foldLeft(field)((f, n) =>
        f.set(n._1.asInstanceOf[language.DeviceId], n._2.asInstanceOf[Value]),
      ),
    )(f =>
      freshPointer[CSharedData].tap: cField => // TODO: when to free this memory?
        cField._1 = f.default.asInstanceOf[Ptr[CBinaryCodable]]
        cField._2 = CMap(
          collection.mutable.Map
            .from(f.neighborValues.asInstanceOf[collection.immutable.Map[EqPtr, CVoidPtr]])
            .map(_.ptr -> _),
          if f.neighborValues.isEmpty then (_: CVoidPtr, _: CVoidPtr) => false
          else f.neighborValues.head._1.asInstanceOf[EqPtr].equals,
        ),
    )

end NativeFieldBasedSharedData

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object NativeFieldBasedSharedData:

  type CSharedData = CStruct2[
    /* default_value */ Ptr[CBinaryCodable],
    /* neighbor_values */ CMap,
  ]

  @exported("shared_data_to_string")
  def sharedDataToString(sd: Ptr[CSharedData]): CString = Zone:
    // todo: free memory
    val defaultStr = fromCString(sd._1.toStr(sd._1))
    val neighborsStr = sd._2.toScalaMap
      .map(n =>
        n._1.asInstanceOf[EqPtr].ptr.asInstanceOf[Ptr[CBinaryCodable]] -> n._2.asInstanceOf[Ptr[CBinaryCodable]],
      )
      .map((nid, nv) => fromCString(nid.toStr(nid)) + " -> " + fromCString(nv.toStr(nv)))
      .toList
      .sorted
      .mkString("[", ", ", "]")
    strdup(toCString(s"Field($defaultStr, $neighborsStr)"))
end NativeFieldBasedSharedData
