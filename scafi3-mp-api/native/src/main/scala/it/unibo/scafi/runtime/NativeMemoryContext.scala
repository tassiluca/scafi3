package it.unibo.scafi.runtime

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ CVoidPtr, Zone }

/**
 * A memory-safe context implementation for native platforms using `Zone` for scoped memory management.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeMemoryContext extends MemorySafeContext:

  override type Arena = Zone

  override def safelyRun[T](block: (Arena, Allocator) ?=> T): T =
    val allocator = new Allocator:
      def free(obj: Any): Unit = stdlib.free(obj.asInstanceOf[CVoidPtr])
    Zone(block(using summon[Arena], allocator))
