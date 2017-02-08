package fakeio

import cats.Monad

final class Auth[F[_]](
  implicit monad: Monad[F], interact: Interact[F]) {
  import Auth.{ apply => _, _ }

  private val passwdDb =
    Map(Id("bob") -> Password("1234"), Id("jim") -> Password("pass"))

  def login: F[User] =
    interact.tell("Welcome to the system!") >> {
    interact.ask("User ID: ") >>= { i =>
    interact.tell(i) >> {

    val id = Id(i)
    val expectedPasswd = passwdDb.getOrElse(id, throw new WrongId)

    interact.ask("Password: ") >>= { p =>

    val passwd = Password(p)

    if (passwd == expectedPasswd) new User(id, passwd).pure
    else throw new WrongPassword
    }}}}
}

object Auth {
  class WrongId extends Throwable
  class WrongPassword extends Throwable

  // For type safety :-)

  case class Id(unwrap: String) extends AnyVal
  object Id {
    implicit val ordering: Ordering[Id] = Ordering by (_.unwrap)
  }

  case class Password(unwrap: String) extends AnyVal
  class User(id: Id, password: Password)

  /** Builds the implicit at compile time. */
  implicit def apply[F[_]: Monad: Interact]: Auth[F] = new Auth
}
