package it.unibo.scafi.runtime

/**
 * A context where memory safety is enforced by construction by the underlying platform (e.g., JVM, JavaScript).
 */
trait NoMemorySafeContext extends MemorySafeContext:

  type Arena = Unit

  object NoAllocator extends Allocator:
    def free(obj: Any): Unit = ()

  inline override def safelyRun[R](f: (Arena, Allocator) ?=> R): R =
    f(using (), NoAllocator)
