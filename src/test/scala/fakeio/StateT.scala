package fakeio

import scalaz.{ Functor, Monad, MonadPlus }

class StateT[S, M[_], A](
  val run: S => M[(A, S)])(implicit functorM: Functor[M]) {
  def eval(s: S): M[A] = functorM.map(run(s))(_._1)
  def exec(s: S): M[S] = functorM.map(run(s))(_._2)
}

object StateT {
  type LocalFunctor[S, M[_]] =
    Functor[({ type StateTSM[A] = StateT[S, M, A] })#StateTSM]

  type LocalMonad[S, M[_]] =
    Monad[({ type StateTSM[A] = StateT[S, M, A] })#StateTSM]

  type LocalMonadPlus[S, M[_]] =
    MonadPlus[({ type StateTSM[A] = StateT[S, M, A] })#StateTSM]

  implicit def monadPlus[S, M[_]](
    implicit monadPlusM: MonadPlus[M]): LocalMonadPlus[S, M] =
    new LocalMonadPlus[S, M] {
      override def bind[A, B](
        fa: StateT[S, M, A])(
        f: A => StateT[S, M, B]): StateT[S, M, B] = {
        val run = { s: S =>
          monadPlusM.bind(fa.run(s)) { case (a, s2) => f(a).run(s2) }
        }

        new StateT(run)
      }

      override def point[A](a: => A): StateT[S, M, A] = {
        val run = { s: S => monadPlusM.point(a -> s) }
        new StateT(run)
      }

      override def empty[A]: StateT[S, M, A] = {
        val run = { _: S => monadPlusM.empty[(A, S)] }
        new StateT(run)
      }

      override def plus[A](
        a: StateT[S, M, A], b: => StateT[S, M, A]): StateT[S, M, A] = {
        val run = { s: S => monadPlusM.plus(a.run(s), b.run(s)) }
        new StateT(run)
      }
    }

  def get[S, M[_]](implicit monadM: Monad[M]): StateT[S, M, S] = {
    val run = { s: S => monadM.point(s -> s) }
    new StateT(run)
  }

  def put[S, M[_]](
    s: S)(implicit monadM: Monad[M]): StateT[S, M, Unit] = {
    val run = { s: S => monadM.point(unit -> s) }
    new StateT(run)
  }

  def modify[S, M[_]](
    f: S => S)(
    implicit monadM: Monad[M],
    monadStateT: LocalMonad[S, M]): StateT[S, M, Unit] =
    monadStateT.bind(get) { s => put(f(s)) }

  def gets[S, M[_], T](
    f: S => T)(
    implicit monadM: Monad[M],
    functorStateT: LocalFunctor[S, M]): StateT[S, M, T] =
    functorStateT.map(get)(f)
}
