package it.unibo.field4s.abstractions

import it.unibo.field4s.UnitTest

import cats.Functor
import cats.syntax.all.*

trait MappableTests:
  this: UnitTest =>
  type F[_]
  def mappable: Functor[F]
  def toIterable[A](fa: F[A]): Iterable[A]

  def mappable(sut: F[Int]): Unit =
    given Functor[F] = mappable
    it should "allow mapping values of collection" in:
      toIterable(summon[Functor[F]].map(sut)(_ + 1)) should be(toIterable(sut.map(_ + 1)))
