package fakeio

import cats.Monad

case class FakeIo[A](
  stdin: List[String], stdout: List[String], result: A)

object FakeIo {
  implicit val monad: Monad[FakeIo] =
    new Monad[FakeIo] {
      override def flatMap[A, B](
        fa: FakeIo[A])(f: (A) => FakeIo[B]): FakeIo[B] =
        f(fa.result)

      override def tailRecM[A, B](
        a: A)(f: (A) => FakeIo[Either[A, B]]) = ???

      override def pure[A](a: A): FakeIo[A] =
        FakeIo(List.empty, List.empty, a)
    }
}
