package it.unibo.scafi.runtime

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ CVoidPtr, Ptr }

import it.unibo.scafi.types.{ Arena, MemorySafeContext }
import it.unibo.scafi.utils.CUtils.freshPointer

class ZoneBasedArena extends Arena:
  override type ManagedObject = CVoidPtr

  inline override def dispose(obj: ManagedObject): Unit = stdlib.free(obj)

/**
 * A memory-safe context implementation for native platforms using `Zone` for scoped memory management.
 */
trait NativeMemoryContext extends MemorySafeContext:

  override type ArenaCtx = ZoneBasedArena

  inline override def safelyRun[T](block: ArenaCtx ?=> T): T = block(using ZoneBasedArena())

  inline def allocateTracking[T](using arena: ArenaCtx): Ptr[T] =
    val ptr = freshPointer[T]
    arena.track(ptr)(arena.dispose)
    ptr
