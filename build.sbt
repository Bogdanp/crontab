name := "crontab"

organization := "io.defn"

version := "0.0.1"

scalaVersion in ThisBuild := "2.11.8"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard"
)

libraryDependencies ++= Seq(
  "org.parboiled" %% "parboiled" % "2.1.3",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
