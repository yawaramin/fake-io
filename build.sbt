name := "fake-io"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++=
  Seq(
    "io.monix" %% "monix-eval" % "2.2.1",
    "io.monix" %% "monix-cats" % "2.2.1",
    "org.specs2" %% "specs2-core" % "3.8.4" % Test)

scalacOptions ++=
  Seq(
    "-feature",
    "-language:higherKinds",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-value-discard")

scalacOptions in Test ++= Seq("-Yrangepos")
