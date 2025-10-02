package it.unibo.scafi.types

import scala.collection.mutable
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.scalanative.unsafe.{ CFuncPtr0, CFuncPtr1, CFuncPtr2, CFuncPtr3, CVoidPtr }

import it.unibo.scafi.libraries.PortableTypes

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
trait NativeTypes extends PortableTypes:

  /*
   * On native generic types are not supported, everything falls back to `void*`, i.e., `CVoidPtr`.
   */

  override type Map[K, V] = CMap
  override given [K, V] => Iso[Map[K, V], collection.immutable.Map[K, V]] =
    Iso[Map[K, V], collection.immutable.Map[K, V]](_.toScalaMap.asInstanceOf[collection.immutable.Map[K, V]])(m =>
      if m.isEmpty then CMap.empty
      else
        CMap(
          mutable.Map.from(m.asInstanceOf[collection.immutable.Map[EqPtr, CVoidPtr]].map(_.ptr -> _)),
          m.head._1.asInstanceOf[EqPtr].equals,
          m.head._1.asInstanceOf[EqPtr].hash,
        ),
    )

  override type Outcome[T] = T
  override given [T] => Iso[Outcome[T], Future[T]] =
    Iso[Outcome[T], Future[T]](Future.successful)(Await.result(_, Duration.Inf))

  /* WARNING
   * =======
   * Note that the following conversions for portable functions needs to be inlined to work properly since
   * the native compiler needs to generate type information (`Tag` type class) in the call site for the conversion
   * to work properly.
   * If the `inline` keyword is removed, compilation completes successfully but runtime execution leads to segfaults!
   */

  override type Function0[R] = CFuncPtr0[R]
  given toScalaFunction0[R]: Conversion[Function0[R], () => R] with
    inline def apply(f: Function0[R]): () => R = f.apply

  override type Function1[T1, R] = CFuncPtr1[T1, R]
  given toScalaFunction1[T1, R]: Conversion[Function1[T1, R], T1 => R] with
    inline def apply(f: Function1[T1, R]): T1 => R = f.apply

  override type Function2[T1, T2, R] = CFuncPtr2[T1, T2, R]

  given toScalaFunction2[T1, T2, R]: Conversion[Function2[T1, T2, R], (T1, T2) => R] with
    inline def apply(f: Function2[T1, T2, R]): (T1, T2) => R = f.apply

  override type Function3[T1, T2, T3, R] = CFuncPtr3[T1, T2, T3, R]

  given toScalaFunction3[T1, T2, T3, R]: Conversion[Function3[T1, T2, T3, R], (T1, T2, T3) => R] with
    inline def apply(f: Function3[T1, T2, T3, R]): (T1, T2, T3) => R = f.apply
end NativeTypes
