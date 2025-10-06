package it.unibo.scafi.types

import cats.kernel.Hash

final class EqWrapper[T: Hash](val value: T):
  override def equals(obj: Any): Boolean = obj match
    case that: EqWrapper[T] @unchecked => Hash[T].eqv(this.value, that.value)
    case _ => false

  override def hashCode(): Int = Hash[T].hash(value)

  override def toString(): String = s"EqWrapper($value)"
