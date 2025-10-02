package it.unibo.scafi.runtime

import scala.scalanative.unsafe.Zone

trait NativeMemoryContext extends MemorySafeContext:

  override type Arena = Zone

  inline override def safelyRun[T](block: Zone ?=> T): T = Zone(block)
