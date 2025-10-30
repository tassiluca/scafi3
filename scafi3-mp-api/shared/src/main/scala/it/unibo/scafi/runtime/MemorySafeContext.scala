package it.unibo.scafi.runtime

trait Allocator:
  private var tracked = Set.empty[Any]
  def track(obj: Any) = tracked += obj
  def collect(): Unit =
    tracked.foreach(free)
    tracked = Set.empty
  def free(obj: Any): Unit

/**
 * A context allowing executing memory-safe operations within a scoped memory region.
 */
trait MemorySafeContext:
  export it.unibo.scafi.runtime.Allocator

  /**
   * Represents a memory-safe allocation scope for the platform in use guaranteeing automatic memory management.
   */
  type Arena

  /**
   * Executes the given block safely within an `Arena` context.
   * @param block
   *   the code block to execute
   * @return
   *   the result of the block execution
   */
  def safelyRun[T](block: (Arena, Allocator) ?=> T): T

  def track(using allocator: Allocator)(obj: Any): Unit = allocator.track(obj)

  def collect(using allocator: Allocator): Unit = allocator.collect()
end MemorySafeContext
