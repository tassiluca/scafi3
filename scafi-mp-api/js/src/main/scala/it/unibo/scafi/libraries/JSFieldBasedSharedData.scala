package it.unibo.scafi.libraries

import scala.scalajs.js
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.types.PortableTypes
import cats.kernel.Hash
import it.unibo.scafi.message.primitive.PrimitiveCodables.asPrimitiveCodable
import it.unibo.scafi.message.JSCodable
import it.unibo.scafi.utils.JSUtils.toByteArray
import it.unibo.scafi.types.EqWrapper

/**
 * A custom portable definition of a field-based `SharedData` structure for js platform.
 */
trait JSFieldBasedSharedData extends PortableLibrary:
  self: PortableTypes & PortableExchangeCalculusLibrary =>

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  override type SharedData[Value] = Field[js.Any, Value]

  /**
   * A portable definition of a `SharedData` structure for js platform based on fields, isomorphic to the
   * [[FieldBasedSharedData]] one.
   * @param default
   *   the default value for unaligned devices
   * @param neighborValues
   *   the values for all devices, aligned and unaligned
   */
  @JSExportAll
  case class Field[ID, Value](default: Value, neighborValues: Map[ID, Value]):
    valueCodable.register(default)
    override def toString(): String = s"Field($default, ${neighborValues.toMap})"

  @JSExport
  @JSExportAll
  object Field:
    /**
     * @param default
     *   the default value for the field
     * @return
     *   a new [[Field]] instance with the given default value and no neighbor values.
     */
    def of[Value](default: Value): Field[js.Any, Value] = Field(default, Map.empty)

  given Hash[js.Any] = new Hash[js.Any]:
    override def hash(x: js.Any): Int = x.hashCode()
    override def eqv(x: js.Any, y: js.Any): Boolean = (
      x.asPrimitiveCodable.getOrElse(JSCodable(x.asInstanceOf[js.Object])).encode(x),
      y.asPrimitiveCodable.getOrElse(JSCodable(y.asInstanceOf[js.Object])).encode(y),
    ) match
      case (encodedX: js.typedarray.Uint8Array, encodedY: js.typedarray.Uint8Array) =>
        encodedX.toByteArray.sameElements(encodedY.toByteArray)
      case _ =>
        scribe.error("Cannot yet compare non-primitive values")
        false

  override given [Value]: Iso[SharedData[Value], language.SharedData[Value]] =
    Iso[SharedData[Value], language.SharedData[Value]](jsField =>
      val field = language.sharedDataApplicative.pure(jsField.default)
      jsField.neighborValues.foldLeft(field)((f, n) => f.set(EqWrapper(n._1).asInstanceOf[language.DeviceId], n._2)),
    )(scalaField =>
      val nvalues: Map[language.DeviceId, Value] =
        scalaField.neighborValues.map((id, v) => (deviceIdConv(id), v)).toMap
      Field(scalaField.default, nvalues.asInstanceOf[Map[js.Any, Value]]),
    )

end JSFieldBasedSharedData
