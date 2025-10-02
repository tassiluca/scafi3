package it.unibo.scafi.runtime

trait NoMemorySafeContext extends MemorySafeContext:

  type Arena = Unit

  inline override def safelyRun[R](f: Arena ?=> R): R = f(using ())
