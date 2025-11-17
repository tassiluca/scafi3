package it.unibo.scafi.runtime

import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ CVoidPtr, Ptr }
import scala.util.chaining.scalaUtilChainingOps

import it.unibo.scafi.types.{ Allocator, MemorySafeContext }
import it.unibo.scafi.utils.CUtils.freshPointer

/**
 * A memory allocator for native platforms that uses `stdlib.free` to deallocate memory.
 */
class NativeAllocator extends Allocator:

  override type ManagedObject = CVoidPtr

  inline override def dispose(obj: ManagedObject): Unit = stdlib.free(obj)

/**
 * A memory-safe context implementation for native platforms using `Zone` for scoped memory management.
 */
trait NativeMemoryContext extends MemorySafeContext:

  override type Arena = NativeAllocator

  inline override def safelyRun[T](block: Arena ?=> T): T = block(using NativeAllocator())

  inline def allocateTracking[T](using arena: Arena): Ptr[T] =
    freshPointer[T].tap(ptr => arena.track(ptr)(arena.dispose))
