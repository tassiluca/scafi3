package it.unibo.scafi.runtime

import scala.scalanative.unsafe.Zone

trait AutoMemoryAllocator extends MemorySafeContext:

  override type Context = Zone

  override def safelyRun[T](block: Zone ?=> T): T = Zone(block)
