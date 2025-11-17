package it.unibo.scafi.runtime

import it.unibo.scafi.types.{ Allocator, MemorySafeContext }

/**
 * A context where memory safety is enforced by construction by the underlying platform (e.g., JVM, JavaScript) and no
 * explicit memory management is needed.
 */
trait NoMemorySafeContext extends MemorySafeContext:

  override type Arena = TransparentAllocator

  class TransparentAllocator extends Allocator:
    override type ManagedObject = Any
    inline override def dispose(obj: Any): Unit = ()

  inline override def safelyRun[R](f: Arena ?=> R): R = f(using TransparentAllocator())
