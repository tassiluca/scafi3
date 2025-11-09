package it.unibo.scafi.libraries

import it.unibo.scafi.language.fc.syntax.FieldCalculusSyntax
import it.unibo.scafi.types.PortableTypes

trait PortableFieldCalculusLibrary extends PortableLibrary:
  self: PortableTypes =>

  override type Language <: AggregateFoundation & FieldCalculusSyntax

  @JSExport
  def evolve[Value](initial: Value)(evolution: Function1[Value, Value]): Value = evolve_(initial)(evolution)

  inline def evolve_[Value](initial: Value)(evolution: Function1[Value, Value]): Value =
    language.evolve(initial)(evolution)

  @JSExport
  def share[Value](initial: Value)(shareAndReturning: Function1[SharedData[Value], Value]): Value =
    share_(initial)(shareAndReturning)

  inline def share_[Value](initial: Value)(shareAndReturning: Function1[SharedData[Value], Value]): Value =
    language.share(initial)(shareAndReturning(_))(using valueCodable(initial))

  @JSExport
  def neighborValues[Value](expr: Value): SharedData[Value] = language.neighborValues(expr)(using valueCodable(expr))
end PortableFieldCalculusLibrary
