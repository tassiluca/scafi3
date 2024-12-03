package it.unibo.field4s.language.foundation

import it.unibo.field4s.abstractions.Aggregate
import it.unibo.field4s.collections.SafeIterable

import cats.Applicative

trait AggregateFoundation:
  type AggregateValue[T] <: SafeIterable[T]

  /**
   * Aggregate values can be iterated also by ignoring the self value.
   */
  given aggregate: Aggregate[AggregateValue]

  /**
   * Aggregate values can be composed and mapped.
   */
  given liftable: Applicative[AggregateValue]
