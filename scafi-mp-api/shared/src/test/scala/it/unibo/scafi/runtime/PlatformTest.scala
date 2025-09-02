package it.unibo.scafi.runtime

import scala.util.Try

import it.unibo.scafi.runtime.PlatformTest.{ Path, Pattern }
import it.unibo.scafi.runtime.PlatformTest.SubstitutionBuilder

import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should
import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.string.{ EndWith, StartWith }

trait PlatformTest extends should.Matchers with FileSystem:
  export PlatformTest.{ ->, Path }

  val snapshotsFolderPath: Path

  val templatePaths: Set[Path]

  val testResourcesPath: Path

  def test(testName: String)(addSubstitutions: SubstitutionBuilder ?=> Unit): Assertion =
    given builder: SubstitutionBuilder = SubstitutionBuilder()
    addSubstitutions
    (for
      workingDir = testDir(testName)
      _ <- resolveTemplates(testName, workingDir, builder.all)
      _ <- compile(workingDir)
      out <- run(workingDir)
      _ = scribe.info(s"Test output: $out")
    yield out).fold(err => fail(s"Test failed: ${err.getMessage}"), out => compare(out, testName))

  def testDir(directoryName: String): Path

  def resolveTemplates(testName: String, workingDir: Path, substitutions: Set[(Pattern, String)]): Try[Unit] = Try:
    templatePaths.foreach(template => copy(template, s"$workingDir/${baseName(template)}"))
    val programContent = read(programContentPath(testName))
    val templateContent = read(programTemplatePath)
    val updatedContent = substitutions.foldLeft(templateContent.appendedAll(programContent)):
      case (content, (pattern, replacement)) =>
        val newContent = content.replace(pattern, replacement)
        if newContent == content then throw IllegalStateException(s"No pattern ${pattern} found in content.")
        else newContent
    write(s"$workingDir/main.mjs", updatedContent)

  def programContentPath(testName: String): Path

  def programTemplatePath: Path

  def compile(workingDir: Path): Try[Unit]

  def run(workingDir: Path): Try[String]

  def compare(actual: String, testName: String): Assertion =
    val expected = Try(read(s"$snapshotsFolderPath/$testName"))
    if expected.isFailure then fail(s"Missing snapshot for test $testName")
    else expected.get.trim shouldBe actual.trim

end PlatformTest

trait FileSystem:

  def copy(origin: Path, destination: Path): Unit

  def baseName(path: Path): String

  def read(path: Path): String

  def write(path: Path, content: String): Unit

object PlatformTest:
  type Path = String

  type Pattern = String :| (StartWith["{{"] & EndWith["}}"])

  class SubstitutionBuilder:
    private var substitutions = Set.empty[(Pattern, String)]
    private[PlatformTest] def add(key: Pattern, value: String): Unit = substitutions += ((key, value))
    def all: Set[(Pattern, String)] = substitutions.view.toSet

  extension (pattern: Pattern)
    infix inline def ->(value: String)(using builder: SubstitutionBuilder): Unit = builder.add(pattern, value)
