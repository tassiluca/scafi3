package it.unibo.scafi.libraries

import it.unibo.scafi.language.fc.syntax.FieldCalculusSyntax
import it.unibo.scafi.types.{ MemorySafeContext, PortableTypes }

trait PortableFieldCalculusLibrary extends PortableLibrary:
  self: PortableTypes & MemorySafeContext =>

  override type Language <: AggregateFoundation & FieldCalculusSyntax

  @JSExport
  def evolve[Value](initial: Value)(evolution: Function1[Value, Value]): Value

  @JSExport
  def share[Value](initial: Value)(shareAndReturning: Function1[SharedData[Value], Value])(using Arena): Value

  @JSExport
  def neighborValues[Value](expr: Value)(using Arena): SharedData[Value]
end PortableFieldCalculusLibrary
