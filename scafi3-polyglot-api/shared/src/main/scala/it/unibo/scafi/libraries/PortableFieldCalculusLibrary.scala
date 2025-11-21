package it.unibo.scafi.libraries

import it.unibo.scafi.language.fc.syntax.FieldCalculusSyntax
import it.unibo.scafi.types.PortableTypes

trait PortableFieldCalculusLibrary extends PortableLibrary:
  self: PortableTypes =>

  override type Language <: AggregateFoundation & FieldCalculusSyntax

  @JSExport
  def evolve[Value](initial: Value)(evolution: Function1[Value, Value]): Value

  @JSExport
  def share[Format, Value <: Codec[Value, Format]](
      initial: Value,
  )(shareAndReturning: Function1[SharedData[Value], Value]): Value

  @JSExport
  def neighborValues[Format, Value <: Codec[Value, Format]](expr: Value): SharedData[Value]
end PortableFieldCalculusLibrary
