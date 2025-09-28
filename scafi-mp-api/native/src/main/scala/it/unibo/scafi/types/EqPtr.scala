package it.unibo.scafi.types

import scala.scalanative.unsafe.{ CBool, CFuncPtr2, CVoidPtr }

/**
 * A C function pointer that determines equality between two C void pointers.
 */
type CEquals = CFuncPtr2[CVoidPtr, CVoidPtr, CBool]

/**
 * A wrapper around a generic pointer that defines equality based on a provided criterion in place of reference
 * equality. This is useful to wrap generic value (pointers) that need to be compared based on custom logic, like
 * content equality in JVM classes (`equals()`) instead of reference equality.
 * @param ptr
 *   the wrapped pointer
 * @param equals
 *   a function pointer to compare two pointers for equality. This function should return true if the
 */
class EqPtr(val ptr: CVoidPtr, val equals: CEquals):
  override def equals(obj: Any): Boolean = obj match
    case other: EqPtr => equals(this.ptr, other.ptr)
    case _ => false
  override def hashCode(): Int = 42 // todo: consistent with equals but bad for hash distribution
  override def toString: String = s"EquivalentPtr($ptr)"
