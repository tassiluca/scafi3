package it.unibo.scafi.types

import java.util.concurrent.atomic.AtomicReference

/**
 * Represents a memory-safe allocation scope for the platform in use guaranteeing automatic memory management.
 */
trait Arena:

  type Object

  private val trackedObjects: AtomicReference[Set[Object]] = new AtomicReference(Set.empty)

  def track(obj: Object): Unit =
    val _ = trackedObjects.updateAndGet(_ + obj)

  def collect(): Unit =
    val current = trackedObjects.getAndSet(Set.empty)
    current.foreach(free)

  def free(obj: Object): Unit

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
