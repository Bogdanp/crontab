name := "crontab"

organization := "io.defn"

version := "0.1.0"

crossScalaVersions := Seq("2.11.8", "2.12.1")
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

publishTo := {
  val nexus = "https://oss.sonatype.org/"

  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
