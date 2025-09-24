package it.unibo.scafi.utils

import scala.reflect.ClassTag
import scala.scalanative.libc.stdlib
import scala.scalanative.unsafe.{ sizeOf, Ptr }

/** A bunch of utilities for C interop. */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf", "scalafix:DisableSyntax.null"))
object CUtils:

  inline def freshPointer[T]: Ptr[T] = freshPointer[T](1)

  /**
   * Allocates memory for a type `T`, possibly multiplied by a factor, returning a pointer to it.
   * @param factor
   *   the factor to multiply the size of the pointer by
   * @tparam T
   *   the type of the pointer
   * @return
   *   a [[Ptr]] pointing to the allocated memory
   */
  inline def freshPointer[T](factor: Int): Ptr[T] =
    requireNonNull(stdlib.malloc(sizeOf[T] * factor).asInstanceOf[Ptr[T]])

  /**
   * Check whether an object is null, throwing a [[NullPointerException]] if it is.
   * @param obj
   *   the object to check
   * @tparam T
   *   the type of the object
   * @return
   *   the object if it is not null
   */
  inline def requireNonNull[T](obj: T): T =
    if obj.asInstanceOf[AnyRef] == null then throw new NullPointerException(s"Object $obj is null") else obj

  /**
   * Safely execute a block of code, logging any exceptions that are thrown.
   * @param block
   *   the block of code to execute
   * @tparam T
   *   the return type of the block
   * @return
   *   the result of the block
   */
  inline def withLogging[T](block: => T): T =
    try block
    catch
      case e =>
        scribe.error(e.getMessage)
        stdlib.exit(1).asInstanceOf[T]
end CUtils
