package fakeio

import java.io.File
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.higherKinds
import scalaz.Monad

/**
  * Represents a 'filesystem' effect which can be specialised by a type
  * of effect.
  *
  * @param monad provides compile-time proof that whatever effect we use
  *              can be sequenced.
  *
  * @param ctx provides extra information that may be needed by the
  *            effect type. Most commonly we need an ExecutionContext
  *            for Scala Futures; but we can also imagine a
  *            SparkContext, etc.
  *
  * @tparam Eff the type of effect we want to control. Most commonly
  *             will be some wrapper type for controlling asynchronous
  *             IO.
  *
  * @tparam Ctx see `ctx`.
  */
abstract class FileSystem[Eff[_], Ctx]()(
  implicit monad: Monad[Eff], ctx: Ctx) {
  /** Returns a list of files in the given directory. */
  def ls(dir: String): Eff[Seq[String]]

  /**
    * Deletes the file if it exists in the directory; otherwise does
    * nothing.
    */
  def rm(dir: String, fileName: String): Eff[Unit]
}

object FileSystem {
  private final class Async()(
    implicit monad: Monad[Future], ctx: ExecutionContext)
    extends FileSystem[Future, ExecutionContext]()(monad, ctx) {
    override def ls(dir: String): Future[Seq[String]] =
      Future(new File(dir).list)

    override def rm(dir: String, fileName: String): Future[Unit] =
      for {
        fileNames <- ls(dir)
        _ <-
          if (fileNames contains fileName)
            Future(new File(fileName).delete)
          else Future successful unit
      } yield unit
  }

  implicit def async(
    implicit monad: Monad[Future],
    ctx: ExecutionContext): FileSystem[Future, ExecutionContext] =
    new Async()
}
