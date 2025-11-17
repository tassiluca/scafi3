package it.unibo.scafi.runtime

import it.unibo.scafi.types.{ Arena, MemorySafeContext }

class NoArena extends Arena:
  override type ManagedObject = Any
  inline override def dispose(obj: Any): Unit = ()

/**
 * A context where memory safety is enforced by construction by the underlying platform (e.g., JVM, JavaScript).
 */
trait NoMemorySafeContext extends MemorySafeContext:

  override type ArenaCtx = NoArena

  inline override def safelyRun[R](f: ArenaCtx ?=> R): R = f(using NoArena())
