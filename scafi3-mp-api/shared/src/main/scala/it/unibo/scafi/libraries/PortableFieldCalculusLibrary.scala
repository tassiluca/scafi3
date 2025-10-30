package it.unibo.scafi.libraries

import it.unibo.scafi.language.fc.syntax.FieldCalculusSyntax
import it.unibo.scafi.runtime.MemorySafeContext
import it.unibo.scafi.types.PortableTypes

trait PortableFieldCalculusLibrary extends PortableLibrary:
  self: PortableTypes & MemorySafeContext =>

  override type Language <: AggregateFoundation & FieldCalculusSyntax

  @JSExport
  def share[Value](initial: Value)(
      shareAndReturning: Function1[SharedData[Value], Value],
  )(using Arena, Allocator): Value = share_(initial)(shareAndReturning)

  inline def share_[Value](initial: Value)(
      shareAndReturning: Function1[SharedData[Value], Value],
  )(using Arena, Allocator): Value = language.share(initial)(shareAndReturning(_))
