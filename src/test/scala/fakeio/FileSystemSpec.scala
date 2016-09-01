package fakeio

import java.io.File
import org.specs2.mutable.Specification
import scalaz.{ Monad, State }

class FileSystemSpec extends Specification {
  import FileSystemSpec._

  "ls" >> {
    "deletes existing file" >> {
      testAction
        .eval(new FileSystemSpec.FakeFile(dir, Seq(fileName)))
        .must(not)
        .contain(fileName)
    }

    "has no effect if file does not exist" >> {
      val fileNames = Seq("bar", "baz")

      testAction
        .eval(new FileSystemSpec.FakeFile(dir, fileNames))
        .mustEqual(fileNames)
    }
  }
}

object FileSystemSpec {
  /**
    * A convenience that lets us quickly set up files which have the
    * names and children we want to test with.
    */
  final class FakeFile(name: String, children: Seq[String])
    extends File(name) {
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
    * Implementation of fake IO that internally keeps track of a fake
    * 'file'.
    *
    * @param monad provides compile-time proof that whatever effect we
    *              use can be sequenced.
    *
    */
  private final class Fake()(implicit monad: Monad[FakeIO])
    extends FileSystem[FakeIO, Unit]() {
    /**
      * Returns the children files of the current state of the tracked
      * file.
      */
    override def ls(dir: String): FakeIO[Seq[String]] =
      State gets (_.list)

    /**
      * Removes a child file of the currently-tracked directory if it
      * exists, otherwise does nothing.
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

  /**
    * Returns a common test action: deleting a certain file from a
    * certain directory and then listing the files in that directory.
    *
    * @param fs the fake FileSystem implementation to operate in.
    */
  def testAction(
    implicit fs: FileSystem[FakeIO, Unit]): FakeIO[Seq[String]] =
    for (_ <- fs.rm(dir, fileName); files <- fs ls dir) yield files

  /** The fake FileSystem implementation. */
  implicit val fake: FileSystem[FakeIO, Unit] = new Fake()

  val dir: String = "."
  val fileName: String = "foo"
}
