package it.unibo.scafi.runtime

trait MemorySafeContext:

  type Context

  def safelyRun[T](block: Context ?=> T): T
