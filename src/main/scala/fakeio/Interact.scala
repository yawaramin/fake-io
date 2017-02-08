package fakeio

import cats.Id
import monix.eval.Task
import monix.execution.Scheduler

abstract class Interact[F[_]] {
  def tell(msg: String): F[Unit]
  def ask(prompt: String): F[String]
}

object Interact {
  implicit def async(implicit scheduler: Scheduler): Interact[Task] =
    new Interact[Task] {
      override def tell(msg: String): Task[Unit] = Task(println(msg))
      override def ask(prompt: String): Task[String] =
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

  def fake(_stdin: List[String]): Interact[FakeIo] =
    new Interact[FakeIo] {
      private var stdin = _stdin
      private var stdout: List[String] = List.empty

      override def tell(msg: String): FakeIo[Unit] = {
        stdout = msg :: stdout
        FakeIo(stdin, stdout, ())
      }

      override def ask(prompt: String): FakeIo[String] = {
        val result = stdin.head
        stdin = stdin.tail
        stdout = prompt :: stdout

        FakeIo(stdin, stdout, result)
      }
    }
}
