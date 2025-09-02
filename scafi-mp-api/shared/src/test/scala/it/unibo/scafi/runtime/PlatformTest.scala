package it.unibo.scafi.runtime

import scala.util.Try

trait PlatformTest:

  type Path = String

  def test(testName: String): Try[Unit] =
    for
      workingDir = prepare(testName)
      _ <- compile(workingDir)
      out <- run(workingDir)
      _ = scribe.info(s"Test output: $out")
    yield ()

  def prepare(testName: String): Path =
    val workingDir = testDir(testName)
    resolveTemplates(testName, workingDir)
    workingDir

  def testDir(directoryName: String): Path

  def resolveTemplates(testName: String, workingDir: Path): Unit

  def compile(workingDir: Path): Try[Unit]

  def run(workingDir: Path): Try[String]
end PlatformTest
