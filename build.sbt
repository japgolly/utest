import sbtcrossproject.{crossProject, CrossType}
import com.typesafe.sbt.pgp.PgpKeys._
import sbt.Keys.scalacOptions
import sbt.addCompilerPlugin
import sbt.librarymanagement.{SemanticSelector, VersionNumber}
import Lib._

val scala212 = "2.12.11"
val scala213 = "2.13.1"

name               in ThisBuild := "utest"
organization       in ThisBuild := "com.github.japgolly.fork"
scalaVersion       in ThisBuild := scala213
crossScalaVersions in ThisBuild := Seq(scala212, scala213)
updateOptions      in ThisBuild := (updateOptions in ThisBuild).value.withCachedResolution(true)
incOptions         in ThisBuild := (incOptions in ThisBuild).value.withLogRecompileOnMacro(false)

lazy val utest = crossProject(JSPlatform, JVMPlatform)
  .settings(
    name                  := "utest",
    scalacOptions         := Seq(
                               "-opt:l:inline",
                               "-opt-inline-from:scala.**",
                               "-opt-inline-from:utest.**",
                               "-Ywarn-dead-code",
                               "-feature"),
    scalacOptions in Test -= "-Ywarn-dead-code",
    libraryDependencies   += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    scalacOptions        ++= (scalaVersion.value match {
      case x if x startsWith "2.13." => Nil
      case x if x startsWith "2.12." => Nil
    }),

    testFrameworks += new TestFramework("test.utest.CustomFramework"),

    // Release settings
    publishArtifact in Test := false,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    homepage := Some(url("https://github.com/japgolly/utest")),
    scmInfo := Some(ScmInfo(
      browseUrl = url("https://github.com/japgolly/utest"),
      connection = "scm:git:git@github.com:japgolly/utest.git"
    )),
    licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
    developers += Developer(
      email = "haoyi.sg@gmail.com",
      id = "lihaoyi",
      name = "Li Haoyi",
      url = url("https://github.com/lihaoyi")
    )//,
//    autoCompilerPlugins := true,
//
//    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.7"),
//
//    scalacOptions += "-P:acyclic:force"

)
  .configureCross(publicationSettings("utest"))
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion,
      "org.portable-scala" %%% "portable-scala-reflect" % "0.1.1"))
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scala-sbt" % "test-interface" % "1.0",
      "org.portable-scala" %%% "portable-scala-reflect" % "0.1.1"))

lazy val root = project.in(file("."))
  .aggregate(utest.js, utest.jvm)
  .settings(
    publishTo := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
    skip in publish := true)
