package it.unibo.scafi.types

import java.util.concurrent.atomic.AtomicReference

/**
 * Represents a memory-safe allocation scope for the platform in use guaranteeing automatic memory management.
 */
trait Arena:

  type Object

  type FreeFn = Object => Unit

  private val trackedObjects: AtomicReference[Map[Object, Option[FreeFn]]] = new AtomicReference(Map.empty)

  def track(obj: Object)(freeFn: FreeFn): Unit =
    val _ = trackedObjects.updateAndGet(_ + (obj -> Some(freeFn)))

  def collect(): Unit =
    val current = trackedObjects.getAndSet(Map.empty)
    current.foreach: (o, f) =>
      f match
        case Some(fn) => fn(o)
        case None => defaultFree(o)

  def defaultFree(obj: Object): Unit
end Arena

/**
 * A context allowing executing memory-safe operations within a scoped memory region.
 */
trait MemorySafeContext:

  type ArenaCtx <: Arena

  /**
   * Executes the given block safely within an `Arena` context.
   * @param block
   *   the code block to execute
   * @return
   *   the result of the block execution
   */
  def safelyRun[T](block: ArenaCtx ?=> T): T

  inline def collect()(using arena: ArenaCtx): Unit = arena.collect()

end MemorySafeContext
