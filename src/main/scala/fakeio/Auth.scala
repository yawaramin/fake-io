package fakeio

import cats.Monad

abstract class Auth[F[_]: Monad: Interact] { def login: F[Auth.User] }

object Auth {
  class Failure extends Exception

  final class Impl[F[_]](
    implicit monad: Monad[F], interact: Interact[F]) extends Auth[F] {
    private val passwdDb =
      Map(Id("bob") -> Password("1234"), Id("jim") -> Password("pass"))

    private val user: Id => Password => F[User] = id => password =>
      monad pure new User(id, password)

    private def fail[A]: A = throw new Failure

    override def login: F[User] =
      monad.followedBy(interact tell "Welcome to the system!")(
      monad.flatMap(interact ask "User ID: ") { i =>
      monad.followedBy(interact tell i)(
      monad.flatMap(interact ask "Password: ") { p =>

      val id = Id(i)
      val password = Password(p)

      passwdDb get id filter password.== map user(id) getOrElse fail
      })})
  }

  // For type safety :-)

  case class Id(unwrap: String) extends AnyVal
  object Id {
    implicit val ordering: Ordering[Id] = Ordering by (_.unwrap)
  }

  case class Password(unwrap: String) extends AnyVal
  class User(id: Id, password: Password)

  /** Builds the implicit at compile time. */
  implicit def impl[F[_]: Monad: Interact]: Auth[F] = new Impl
}
