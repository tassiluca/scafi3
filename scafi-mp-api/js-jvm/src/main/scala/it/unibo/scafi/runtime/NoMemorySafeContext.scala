package it.unibo.scafi.runtime

trait NoMemorySafeContext extends MemorySafeContext:

  type Context = Unit

  override def safelyRun[R](f: Context ?=> R): R = f(using ())
