package object fakeio {
  type Id[A] = A

  implicit val unit = ()
}
