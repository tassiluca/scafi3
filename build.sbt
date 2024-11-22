import scala.scalanative.build.*

val scala3Version = "3.6.1"

ThisBuild / scalaVersion := scala3Version
ThisBuild / organization := "it.unibo.field4s"
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
ThisBuild / homepage := Some(url("https://github.com/field4s/field4s"))
ThisBuild / licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / developers := List(
  Developer(
    "nicolasfara",
    "Nicolas Farabegoli",
    "nicolas.farabegoli@unibo.it",
    url("https://nicolasfarabegoli.it")
  ),
  Developer(
    "cric96",
    "Gianluca Aguzzi",
    "gianluca.aguzzi@unibo.it",
    url("https://github.com/cric96")
  )
)
ThisBuild / scalacOptions ++= Seq(
  "-Werror",
  "-rewrite",
  "-indent",
  "-unchecked",
  "-explain",
)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("core"))
  .configs()
    .nativeSettings(
      nativeConfig ~= {
        _.withLTO(LTO.default)
          .withMode(Mode.releaseSize)
          .withGC(GC.immix)
      }
    )
    .jsSettings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withOptimizer(true) }
    )
  .settings(
    name := "core",
    sonatypeProfileName := "it.nicolasfarabegoli",
    libraryDependencies ++= Seq()
  )

lazy val check = taskKey[Unit]("Runs all verification tasks like tests, linters, etc.")
check := {
  (core.jvm / Test / test).value
  (core.jvm / Compile / scalafmtCheck).value

  (core.js / Test / test).value
  (core.js / Compile / scalafmtCheck).value

  (core.native / Test / test).value
  (core.native / Compile / scalafmtCheck).value
}

compile := (Compile / compile dependsOn check).value
