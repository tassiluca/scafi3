package it.unibo.scafi.types

/**
 * Represents a memory-safe allocation scope for the platform in use guaranteeing automatic memory management.
 */
trait Arena:

  /** The type of managed objects tracked by this arena. */
  type ManagedObject

  /** The type of disposer functions for managed objects. */
  type Disposer = ManagedObject => Unit

  private var trackedObjects: Map[ManagedObject, Option[Disposer]] = Map.empty

  /**
   * Tracks a managed object for automatic disposal when the arena is collected.
   * @param obj
   *   the managed object to track
   * @param disposer
   *   a custom disposer to use when disposing the object
   */
  def track(obj: ManagedObject)(disposer: Disposer): Unit = synchronized:
    trackedObjects += (obj -> Some(disposer))

  /** Collects and disposes all managed objects tracked by this arena. */
  def collect(): Unit = synchronized:
    trackedObjects.foreach((o, d) => d.fold(dispose)(_(o)))
    trackedObjects = Map.empty

  /**
   * Default disposal action for managed objects.
   * @param obj
   *   the managed object to dispose
   */
  def dispose(obj: ManagedObject): Unit
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

  /**
   * Collects and disposes all managed objects tracked by the current arena.
   * @param arena
   *   the current arena context
   */
  inline def collect()(using arena: ArenaCtx): Unit = arena.collect()

end MemorySafeContext
