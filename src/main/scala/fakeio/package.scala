package object fakeio {
  //type Id[A] = A

  implicit final class PureOps[F[_], A](
    lhs: A)(implicit ev: cats.Monad[F]) {
    def pure: F[A] = ev pure lhs
  }

  implicit final class MonadOps[F[_], A](
    lhs: F[A])(implicit ev: cats.Monad[F]) {
    def >>[B](fb: F[B]): F[B] = ev.followedBy(lhs)(fb)
    def >>=[B](f: A => F[B]): F[B] = ev.flatMap(lhs)(f)
  }
}
