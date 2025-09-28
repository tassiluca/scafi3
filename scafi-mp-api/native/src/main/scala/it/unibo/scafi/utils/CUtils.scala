package it.unibo.scafi.utils

import scala.reflect.ClassTag
import scala.scalanative.libc.stddef.size_t
import scala.scalanative.libc.stdlib.malloc
import scala.scalanative.unsafe.{ alloc, sizeOf, Ptr, Zone }
import scala.scalanative.unsafe.Size.byteToSize
import scala.scalanative.unsigned.UByte

/** A bunch of utilities for C interop. */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf", "scalafix:DisableSyntax.null"))
object CUtils:

  /**
   * Allocates memory for a type `T`, returning a pointer to it.
   * @tparam T
   *   the type of the pointer
   * @return
   *   a [[Ptr]] pointing to the allocated memory
   */
  inline def freshPointer[T]: Ptr[T] = freshPointer[T](1)

  /**
   * Allocates memory for a type `T`, multiplied by a factor, returning a pointer to it.
   * @param factor
   *   the factor to multiply the size of the pointer by
   * @tparam T
   *   the type of the pointer
   * @return
   *   a [[Ptr]] pointing to the allocated memory
   */
  inline def freshPointer[T](factor: Int): Ptr[T] =
    requireNonNull(malloc(sizeOf[T] * factor).asInstanceOf[Ptr[T]])

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

  extension (bytes: Array[Byte])

    /**
     * Convert an array of bytes to a pointer to `uint8_t`.
     * @param bytes
     *   the array of bytes to convert
     * @param z
     *   the zone to allocate the pointer in
     * @return
     *   a pointer to `uint8_t` representing the array of bytes
     * @note
     *   the pointer is allocated in the given zone and will be freed when the zone is closed.
     */
    def toUint8Array(using Zone): Ptr[UByte] =
      val ptr = alloc[UByte](bytes.length)
      for i <- bytes.indices do !(ptr + i) = bytes(i).toUByte
      ptr

  extension (ptr: Ptr[UByte])

    /**
     * Convert a pointer to `uint8_t` to an array of bytes.
     * @param size
     *   the size of the array to convert to
     * @return
     *   an array of bytes representing the pointer to `uint8_t`
     * @note
     *   the pointer is not freed, as it is assumed to be managed elsewhere. It is the caller's responsibility to ensure
     *   that the pointer is valid and points to a memory region of at least `size` bytes.
     */
    def toByteArray(size: size_t): Array[Byte] =
      val array = new Array[Byte](size.toInt)
      for i <- 0 until size.toInt do array(i) = (!(ptr + i)).toByte
      array
end CUtils
