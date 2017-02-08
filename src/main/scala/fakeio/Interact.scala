package fakeio

import monix.eval.Task

abstract class Interact[F[_]] {
  def tell(msg: String): F[Unit]
  def ask(prompt: String): F[String]
}

object Interact {
  implicit val task: Interact[Task] =
    new Interact[Task] {
      override def tell(msg: String) = Task(println(msg))
      override def ask(prompt: String) =
        Task {
          val result = io.StdIn.readLine(prompt)

          println("")
          result
        }
    }

  implicit val sync: Interact[Id] =
    new Interact[Id] {
      override def tell(msg: String): Id[Unit] = println(msg)
      override def ask(prompt: String): Id[String] = {
        val result = io.StdIn.readLine(prompt)

        println("")
        result
      }
    }
}
