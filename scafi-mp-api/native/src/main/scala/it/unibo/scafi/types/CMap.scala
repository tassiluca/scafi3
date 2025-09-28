package it.unibo.scafi.types

import java.util.concurrent.ConcurrentHashMap

import scala.language.unsafeNulls
import scala.scalanative.unsafe.{ exported, CBool, CFuncPtr2, CSize, CVoidPtr }
import scala.scalanative.unsafe.Size.intToSize

/**
 * A generic, C-interoperable map of elements. This map is heterogeneous: keys and values can be of different types.
 * Internally, it only stores raw `void*` pointers to the actual values, so callers are responsible for ensuring that
 * the pointers are of coherent types. Dereferencing or misinterpreting these pointers can result in undefined or unsafe
 * behavior.
 * @note
 *   Unlike Scala's `Map`, this container does not enforce type safety at compile time and is intended primarily as a
 *   low-level, flexible utility for C/C++ interoperability.
 * @param underlying
 *   the underlying Scala Native map implementation delegating the operations to
 * @see
 *   companion object for exported functions callable from C/C++
 */
class CMap private (
    underlying: collection.mutable.Map[CVoidPtr, CVoidPtr],
    areEquals: CFuncPtr2[CVoidPtr, CVoidPtr, CBool],
):
  export underlying.{ size, foreach }

  def update(key: CVoidPtr, value: CVoidPtr): Unit =
    findBy(key).fold(underlying.put(key, value))(k => underlying.update(k, value)): Unit

  def get(key: CVoidPtr): CVoidPtr = findBy(key).fold(null: CVoidPtr)(k => underlying(k))

  private def findBy(key: CVoidPtr): Option[CVoidPtr] = underlying.keys.find(areEquals(_, key))

  def toMap: Map[CVoidPtr, CVoidPtr] = underlying.view.toMap
object CMap:

  /* NOTE: Scala objects are tracked by Garbage Collector. To avoid them being collected while still in use from C
   * side (the collector cannot be aware of), we keep a reference to them in this set. */
  private val activeRefs = ConcurrentHashMap.newKeySet[CMap]()

  def of(
      underlying: collection.mutable.Map[CVoidPtr, CVoidPtr],
      areEquals: CFuncPtr2[CVoidPtr, CVoidPtr, CBool],
  ): CMap =
    println("Underlying map has size " + underlying.size)
    val map = empty(areEquals)
    activeRefs.add(map)
    underlying.foreach(map.update)
    println("Created map with size " + map.size)
    map

  @exported("map_empty")
  def empty(compareFun: CFuncPtr2[CVoidPtr, CVoidPtr, CBool]): CMap =
    val map = CMap(collection.mutable.Map.empty, compareFun)
    activeRefs.add(map)
    map

  @exported("map_put")
  def put(map: CMap, key: CVoidPtr, value: CVoidPtr): Unit = map.update(key, value)

  @exported("map_get")
  def get(map: CMap, key: CVoidPtr): CVoidPtr = map.get(key)

  @exported("map_size")
  def size(map: CMap): CSize = map.size.toCSize

  @exported("map_foreach")
  def foreach(map: CMap, f: CFuncPtr2[CVoidPtr, CVoidPtr, Unit]): Unit = map.foreach(f.apply)

  @exported("map_free")
  def free(map: CMap): Unit = activeRefs.remove(map): Unit
end CMap
