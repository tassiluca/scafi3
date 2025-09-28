package it.unibo.scafi.libraries

import scala.scalanative.unsafe.{ CFuncPtr2, CStruct2, CVoidPtr, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.types.CMap
import it.unibo.scafi.utils.CUtils.freshPointer

import libscafi3.all.BinaryCodable
import it.unibo.scafi.types.EqPtr

/**
 * A custom portable definition of a field-based `SharedData` structure for native platform.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeFieldBasedSharedData extends PortableLibrary:
  self: PortableTypes & PortableExchangeCalculusLibrary =>

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  type CNeighborhood = CMap

  type CSharedData = CStruct2[
    /* default_value */ Ptr[BinaryCodable],
    /* neighbor_values */ CNeighborhood,
  ]

  override type SharedData[Value] = Ptr[CSharedData]

  object Field:
    /**
     * @param default
     *   the default value for the field
     * @return
     *   a new [[Field]] instance with the given default value and no neighbor values.
     */
    // @NativeExported
    def of(default: CVoidPtr): SharedData[CVoidPtr] =
      // todo: register to universal codable
      language.sharedDataApplicative.pure(default)

  override given [Value]: Iso[SharedData[Value], language.SharedData[Value]] =
    Iso[SharedData[Value], language.SharedData[Value]](cField =>
      val field = language.sharedDataApplicative.pure((!cField)._1.asInstanceOf[Value])
      (!cField)._2.toScalaMap.foldLeft(field)((f, n) =>
        f.set(n._1.asInstanceOf[language.DeviceId], n._2.asInstanceOf[Value]),
      ),
    )(f =>
      freshPointer[CSharedData].tap: cField => // TODO: when to free this memory?
        cField._1 = f.default.asInstanceOf[Ptr[BinaryCodable]]
        cField._2 = CMap.of(
          collection.mutable.Map
            .from(f.neighborValues.asInstanceOf[collection.immutable.Map[EqPtr, CVoidPtr]])
            .map((k, v) => (k.ptr, v)),
          if f.neighborValues.isEmpty
          then (_: CVoidPtr, _: CVoidPtr) => false
          else f.neighborValues.head._1.asInstanceOf[EqPtr].areEquals,
        ),
    )

end NativeFieldBasedSharedData
