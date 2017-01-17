organization := "io.defn"
name := "crontab"
version := "0.1.0"
licenses := Seq("BSD-3" -> url("https://github.com/Bogdanp/crontab/blob/master/LICENSE"))
homepage := Some(url("https://github.com/Bogdanp/crontab"))

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

publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"

  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }
pomExtra := (
  <scm>
    <url>git@github.com:Bogdanp/crontab.git</url>
    <connection>scm:git:git@github.com:Bogdanp/crontab.git</connection>
  </scm>
  <developers>
    <developer>
      <id>Bogdanp</id>
      <name>Bogdan Popa</name>
      <url>http://defn.io</url>
    </developer>
  </developers>)
