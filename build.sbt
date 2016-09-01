name := "fake-io"

version := "0.0.1"

scalaVersion := "2.11.8"

libraryDependencies ++=
  Seq(
    "org.scalaz" %% "scalaz-core" % "7.2.5",
    "org.specs2" %% "specs2-core" % "3.8.4" % Test)

scalacOptions ++=
  Seq(
    "-feature",
    "-language:higherKinds",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-value-discard",
    "-Ywarn-unused-import")

scalacOptions in Test ++= Seq("-Yrangepos")
