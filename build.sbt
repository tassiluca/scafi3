import bindgen.interface.Binding
import org.scalajs.linker.interface.OutputPatterns
import scala.scalanative.build.*
import sbtcrossproject.CrossProject

val scala3Version = "3.7.2"

ThisBuild / scalaVersion := scala3Version
ThisBuild / name := "scafi3"
ThisBuild / organization := "it.unibo.scafi"
ThisBuild / homepage := Some(url("https://github.com/scafi/scafi3"))
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
  ),
  Developer(
    "davidedomini",
    "Danide Domini",
    "davide.domini@unibo.it",
    url("https://github.com/davidedomini")
  )
)
ThisBuild / scalacOptions ++= Seq(
  "-Werror",
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wnonunit-statement",
  "-Yexplicit-nulls",
  "-Wsafe-init",
  "-Ycheck-reentrant",
  "-Xcheck-macros",
  "-rewrite",
  "-indent",
  "-unchecked",
  "-explain",
  "-encoding", "UTF-8",
  "-feature",
  "-preview",
  "-deprecation",
  "-language:strictEquality",
  "-language:implicitConversions",
  "-language:experimental.saferExceptions",
  "-language:experimental.modularity",
  "-Wconf:msg=unused value of type org.scalatest.Assertion:s",
  "-Wconf:msg=unused value of type org.scalatest.compatible.Assertion:s",
  "-Wconf:msg=unused value of type org.specs2.specification.core.Fragment:s",
  "-Wconf:msg=unused value of type org.specs2.matcher.MatchResult:s",
  "-Wconf:msg=unused value of type org.scalamock:s",
)
ThisBuild / coverageEnabled := true
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

val ExclusiveTestTag = Tags.Tag("exclusive-test")
Global / concurrentRestrictions += Tags.exclusive(ExclusiveTestTag)

lazy val commonDependencies =
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % "2.13.0",
    "org.scalactic" %%% "scalactic" % "3.2.19",
    "io.github.iltotore" %%% "iron" % "3.2.0",
    "com.outr" %%% "scribe" % "3.17.0",
    "dev.optics" %%% "monocle-core" % "3.3.0",
    "dev.optics" %%% "monocle-macro" % "3.3.0",
    "org.scalatest" %%% "scalatest" % "3.2.19" % Test,
    "org.scalatestplus" %%% "scalacheck-1-18" % "3.2.19.0" % Test,
  )

lazy val commonNativeSettings = Seq(
  nativeConfig ~= { defaultConfig =>
    defaultConfig.withLTO(LTO.full)
      .withMode(Mode.releaseSize)
      .withGC(GC.immix)
      .withBuildTarget(BuildTarget.libraryDynamic)
      .withCompileOptions(
        defaultConfig.compileOptions ++ Seq(
          "-Wall",                           // Enable all standard warnings about potential issues
          "-Wextra",                         // Enable extra warnings
          "-Wpedantic",                      // Enforce strict ISO C compliance
          "-Werror",                         // Promote all warnings to compilation errors
          "-fsanitize=address,null,thread",  // Enable sanitizers for detecting heap/stack/Global buffer overflows, 
                                             // null pointer dereferences and thread data races
          "-g",                              // Generate debug information (required by sanitizers)
        )
      )
  },
  coverageEnabled := false,
)

lazy val commonBindgenSettings = Seq(
  scalacOptions ++= Seq(s"-Wconf:msg=unused import&src=${sourceManaged.value.getAbsolutePath}/.*:silent"),
)

lazy val commonJsSettings = Seq(
  scalaJSLinkerConfig ~= {
    _.withModuleKind(ModuleKind.ESModule)
      .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
      .withOptimizer(true)
  },
  Compile / fastLinkJS / scalaJSLinkerOutputDirectory := target.value / "fastLinkJS",
  Compile / fullLinkJS / scalaJSLinkerOutputDirectory := target.value / "fullLinkJS",
  coverageEnabled := false,
)

lazy val `scafi-core` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("scafi-core"))
  .configs()
  .nativeSettings(commonNativeSettings)
  .jsSettings(commonJsSettings)
  .settings(commonDependencies)
  .settings(
    name := "scafi-core",
  )

lazy val `scafi-distributed` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("scafi-distributed"))
  .dependsOn(`scafi-core` % "compile->compile;test->test")
  .nativeSettings(commonNativeSettings)
  .jsSettings(commonJsSettings)
  .settings(commonDependencies)
  .settings(
    name := "scafi-distributed",
    libraryDependencies ++= Seq(
      "io.bullet" %%% "borer-core" % "1.16.1",
      "io.bullet" %%% "borer-derivation" % "1.16.1",
    ),
  )

lazy val `scafi-mp-api` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("scafi-mp-api"))
  .dependsOn(`scafi-core` % "compile->compile;test->test", `scafi-distributed`)
  .enablePlugins(BindgenPlugin, VcpkgPlugin)
  .nativeSettings(
    commonNativeSettings,
    commonBindgenSettings,
    vcpkgDependencies := VcpkgDependencies("uthash"),
    bindgenBindings ++= Seq(
      Binding(
        header = (Compile / resourceDirectory).value / s"${(ThisBuild / name).value}.h",
        packageName = s"lib${(ThisBuild / name).value}"
      ).withClangFlags(List("-I" + vcpkgConfigurator.value.includes("uthash").toString)).withExport(true)
    )
  )
  .jsSettings(commonJsSettings)
  .settings(commonDependencies)
  .settings(
    name := "scafi-mp-api",
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    ),
  )

lazy val `scafi-integration` = project
  .in(file("scafi-integration"))
  .dependsOn(`scafi-distributed`.jvm % "compile->compile;test->test")
  .settings(commonDependencies)
  .settings(
    publish / skip := true,
    Test / test := (Test / test).dependsOn(`scafi-mp-api`.js / Compile / fullLinkJS).tag(ExclusiveTestTag).value,
  )

//val alchemistVersion = "42.1.0"
//lazy val `alchemist-incarnation-scafi3` = project
//  .settings(
//    fork := true,
//    name := "alchemist-incarnation-scafi3",
//    libraryDependencies ++= Seq(
//      "it.unibo.alchemist" % "alchemist" % alchemistVersion,
//      "it.unibo.alchemist" % "alchemist-swingui" % alchemistVersion,
//      "it.unibo.alchemist" % "alchemist-api" % alchemistVersion,
//      "it.unibo.alchemist" % "alchemist-test" % alchemistVersion,
//    ),
//  )
////  .dependsOn(core.jvm)
//  .dependsOn(`scafi-core`)

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(
    (
      crossProjects(`scafi-core`, `scafi-distributed`, `scafi-mp-api`)
      ++
      Seq(`scafi-integration` /* :+ `alchemist-incarnation`*/)
    ).map(_.project)*
  )
  .settings(
    name := (ThisBuild / name).value,
    publish / skip := true,
    publishArtifact := false,
  )

def crossProjects(crossProjects: CrossProject*) = crossProjects.flatMap(cp => Seq(cp.js, cp.jvm, cp.native))
