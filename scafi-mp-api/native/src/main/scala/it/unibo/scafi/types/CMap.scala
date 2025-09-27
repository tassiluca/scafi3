package it.unibo.scafi.types

import scala.language.unsafeNulls
import scala.scalanative.unsafe.{ exported, CVoidPtr }
import scala.scalanative.unsafe.CFuncPtr2

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
class CMap(underlying: Map[CVoidPtr, CVoidPtr]) extends scala.collection.immutable.Map[CVoidPtr, CVoidPtr]:
  export underlying.{ iterator, get, removed, updated }

object CMap:

  @exported("map_empty")
  def empty: CMap = CMap(Map.empty)

  @exported("map_put")
  def put(map: CMap, key: CVoidPtr, value: CVoidPtr): CMap = CMap(map.updated(key, value))

  @exported("map_remove")
  def remove(map: CMap, key: CVoidPtr): CMap = CMap(map.removed(key))

  @exported("map_get")
  def get(map: CMap, key: CVoidPtr): CVoidPtr = map.get(key).orNull

  @exported("map_foreach")
  def foreach(map: CMap, f: CFuncPtr2[CVoidPtr, CVoidPtr, Unit]): Unit =
    println(s"> [map] map size: ${map.size}")
    map.iterator.foreach((k, v) => f(k, v))
    println(s"> [map] iteration done.")
end CMap
