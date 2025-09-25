package it.unibo.scafi.libraries

import it.unibo.scafi.language.xc.FieldBasedSharedData

/**
 * A custom portable definition of a field-based `SharedData` structure for js platform.
 */
trait JSFieldBasedSharedData extends PortableLibrary:
  self: PortableTypes & PortableExchangeCalculusLibrary =>

  override type Language <: AggregateFoundation & ExchangeSyntax & FieldBasedSharedData

  override type SharedData[Value] = JSField[Value]

  /**
   * A portable definition of a `SharedData` structure for js platform based on fields, isomorphic to the
   * [[FieldBasedSharedData]] one.
   * @param default
   *   the default value for unaligned devices
   * @param neighborValues
   *   the values for all devices, aligned and unaligned
   */
  @JSExportAll
  case class JSField[Value](default: Value, neighborValues: Map[language.DeviceId, Value]):
    valueCodable.register(default)
    override def toString(): String = s"Field($default, ${neighborValues.toMap})"

  @JSExport("Field")
  @JSExportAll
  object JSField:
    /**
     * @param default
     *   the default value for the field
     * @return
     *   a new [[Field]] instance with the given default value and no neighbor values.
     */
    def of[Value](default: Value): JSField[Value] = JSField(default, Map.empty)

  override given [Value]: Iso[SharedData[Value], language.SharedData[Value]] =
    Iso[SharedData[Value], language.SharedData[Value]](jsField =>
      val field = language.sharedDataApplicative.pure(jsField.default)
      jsField.neighborValues.foldLeft(field)((f, n) => f.set(n._1, n._2)),
    )(f => JSField(f.default, f.neighborValues))

end JSFieldBasedSharedData
