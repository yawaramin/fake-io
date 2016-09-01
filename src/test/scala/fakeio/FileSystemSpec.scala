package fakeio

import org.specs2.mutable.Specification
import scalaz.{ Monad, State }
import scalaz.syntax.monad._

final class FileSystemSpec extends Specification {
  import FileSystemSpec._

  "ls" >> {
    "does not list deleted file" >> {
      deleteThenList[FakeIO, Unit](dir, fileName)
        // Note: eval because `deleteThenList` 'returns' the list.
        .eval(new FakeFile(dir, Seq(fileName)))
        .must(not)
        .contain(fileName)
    }

    "has no effect if file does not exist" >> {
      val fileNames = Seq("bar", "baz")

      deleteThenList[FakeIO, Unit](dir, fileName)
        .eval(new FakeFile(dir, fileNames)).mustEqual(fileNames)
    }
  }
}

object FileSystemSpec {
  /**
    * A convenience that lets us quickly set up files which have the
    * names and children we want to test with.
    */
  final class FakeFile(name: String, children: Seq[String])
    extends java.io.File(name) {
    override def list: Array[String] = children.toArray
  }

  /**
    * A convenience to express that our 'fake IO' is just controlled
    * state mutation.
    *
    * @tparam A the type of return values from evaluating our stateful
    *           computations.
    */
  type FakeIO[A] = State[FakeFile, A]

  /**
    * Returns a common test action: deleting a given file from a given
    * directory and then listing the files in that directory.
    *
    * Note that this is (almost) totally generic: it can be used with
    * any effect and context types as long as we can prove that the
    * effects can be sequenced monadically. To prove this, just remove
    * the `Monad` constraint from the right of the `Eff` type parameter
    * below and try to compile the tests.
    *
    * @param dir the directory to look for the file in.
    * @param fileName the file to delete.
    * @param fs the fake FileSystem implementation to operate in.
    *
    * @tparam Eff the type of effect we are running inside.
    * @tparam Ctx the type of context we need for the effect.
    */
  def deleteThenList[Eff[_]: Monad, Ctx](dir: String, fileName: String)(
    implicit fs: FileSystem[Eff, Ctx]): Eff[Seq[String]] =
    for (_ <- fs.rm(dir, fileName); files <- fs ls dir) yield files

  val dir: String = "."
  val fileName: String = "foo"

  /**
    * The fake FileSystem implementation that internally keeps track of
    * a fake 'file'.
    */
  implicit val fake: FileSystem[FakeIO, Unit] =
    new FileSystem[FakeIO, Unit]() {
      /**
        * Returns a stateful action that evaluates to the children files
        * of the tracked file.
        */
      override def ls(dir: String): FakeIO[Seq[String]] =
        State gets (_.list)

      /**
        * Returns a stateful action that removes a child file of the
        * currently-tracked directory if it exists, otherwise does
        * nothing.
        */
      override def rm(dir: String, fileName: String): FakeIO[Unit] =
        for {
          fileNames <- ls(dir)
          _ <-
            State modify { f: FakeFile =>
              if (fileNames contains fileName)
                new FakeFile(dir, fileNames filterNot fileName.==)
              else f
            }
        } yield unit
    }
}
