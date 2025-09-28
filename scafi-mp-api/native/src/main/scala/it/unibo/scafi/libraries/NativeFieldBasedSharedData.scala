package it.unibo.scafi.libraries

import scala.scalanative.unsafe.{ CFuncPtr2, CStruct2, CVoidPtr, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.types.{ CBinaryCodable, CMap, EqPtr }
import it.unibo.scafi.utils.CUtils.freshPointer

/**
 * A custom portable definition of a field-based `SharedData` structure for native platform.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeFieldBasedSharedData extends PortableLibrary:
  self: PortableTypes & PortableExchangeCalculusLibrary =>

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  type CNeighborhood = CMap

  type CSharedData = CStruct2[
    /* default_value */ Ptr[CBinaryCodable],
    /* neighbor_values */ CNeighborhood,
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
          if f.neighborValues.isEmpty
          then (_: CVoidPtr, _: CVoidPtr) => false
          else f.neighborValues.head._1.asInstanceOf[EqPtr].equals,
        ),
    )

end NativeFieldBasedSharedData
