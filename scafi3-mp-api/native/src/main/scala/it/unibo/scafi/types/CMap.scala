package it.unibo.scafi.types

import java.util.concurrent.atomic.AtomicLong

import scala.collection.mutable
import scala.language.unsafeNulls
import scala.scalanative.unsafe.{ exported, CSize, CVoidPtr, Ptr, UnsafeRichLong }
import scala.scalanative.unsafe.Size.intToSize

/**
 * A facade around a mutable map to be used in C/C++ code via Scala Native. From the C/C++ side, the map is represented
 * as an opaque pointer (`void*`) that acts as a handle to identify the map instance.
 */
object CMap:

  private val activeRefs = mutable.Map.empty[Long, mutable.Map[?, ?]]
  private val nextHandle = AtomicLong(0L)

  def apply[Key, Value](map: mutable.Map[Key, Value]): Ptr[Byte] =
    val handle = nextHandle.incrementAndGet()
    synchronized(activeRefs.update(handle, map))
    handle.toPtr[Byte]

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def of[Key, Value](ptr: Ptr[Byte]): mutable.Map[Key, Value] =
    val handle = ptr.toLong
    synchronized(activeRefs(handle)).asInstanceOf[mutable.Map[Key, Value]]

  @exported("map_empty")
  def empty: Ptr[Byte] = apply(mutable.Map.empty[CVoidPtr, CVoidPtr])

  @exported("map_put")
  def put(handle: Ptr[Byte], key: CVoidPtr, value: CVoidPtr): Unit = of(handle).update(key, value)

  @exported("map_get")
  def get(handle: Ptr[Byte], key: CVoidPtr): CVoidPtr = of(handle).get(key).orNull

  @exported("map_size")
  def size(handle: Ptr[Byte]): CSize = of(handle).size.toCSize

  @exported("map_free")
  def free(handle: Ptr[Byte]): Unit = synchronized(activeRefs.remove(handle.toLong)): Unit
end CMap
