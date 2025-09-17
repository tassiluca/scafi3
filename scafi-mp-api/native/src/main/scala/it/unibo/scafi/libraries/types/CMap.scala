package it.unibo.scafi.libraries.types

import scala.language.unsafeNulls
import scala.scalanative.unsafe.{ exported, CVoidPtr }

class CMap(underlying: Map[CVoidPtr, CVoidPtr]) extends Map[CVoidPtr, CVoidPtr]:
  export underlying.{ iterator, get, removed, updated }

object CMap:

  @exported("map_empty")
  def empty: CMap = CMap(Map.empty)

  @exported("map_put")
  def put(map: CMap, key: CVoidPtr, value: CVoidPtr): CMap = CMap(map.updated(key, value))

  @exported("map_get")
  def get(map: CMap, key: CVoidPtr): CVoidPtr = map.get(key).orNull
