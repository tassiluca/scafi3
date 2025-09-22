package it.unibo.scafi.types

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.scalanative.unsafe.CVoidPtr

import it.unibo.scafi.libraries.PortableTypes

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeTypes extends PortableTypes:

  // On native generic types are not supported, everything falls back to `void*`, i.e., `CVoidPtr`

  override type Map[K, V] = CMap
  override given [K, V] => Iso[Map[K, V], collection.immutable.Map[K, V]] =
    Iso[Map[K, V], collection.immutable.Map[K, V]](
      _.asInstanceOf[collection.immutable.Map[K, V]],
    )(m => CMap(m.asInstanceOf[collection.immutable.Map[CVoidPtr, CVoidPtr]]))

  override type Tuple2[A, B] = CTuple
  override given [A, B] => Iso[Tuple2[A, B], (A, B)] =
    Iso[Tuple2[A, B], (A, B)](_.asInstanceOf[(A, B)])(t => CTuple(t.asInstanceOf[(CVoidPtr, CVoidPtr)]))

  override type Outcome[T] = T
  override given [T] => Iso[Outcome[T], Future[T]] =
    Iso[Outcome[T], Future[T]](Future.successful)(Await.result(_, Duration.Inf))

  override type Function0[R] = () => R
  override given [R] => Conversion[Function0[R], () => R] = _.apply

  override type Function1[T1, R] = T1 => R
  override given [T1, R] => Conversion[Function1[T1, R], T1 => R] = _.apply
end NativeTypes
