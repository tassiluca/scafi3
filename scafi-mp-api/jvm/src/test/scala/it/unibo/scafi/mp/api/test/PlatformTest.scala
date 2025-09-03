package it.unibo.scafi.mp.api.test

import scala.util.Try

import it.unibo.scafi.mp.api.test.PlatformTest.Pattern
import it.unibo.scafi.mp.api.test.PlatformTest.SubstitutionBuilder

import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should
import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.string.{ EndWith, StartWith }
import java.nio.file.{ Files, Path }
import java.nio.charset.StandardCharsets
import java.nio.file.StandardOpenOption

trait PlatformTest extends should.Matchers with FileSystem:
  export PlatformTest.->
  export io.github.iltotore.iron.autoRefine

  val snapshotsFolderPath: Path

  val templatePaths: Set[Path]

  val mainFileName: String

  def testProgram(testName: String)(addSubstitutions: SubstitutionBuilder ?=> Unit): Assertion =
    given builder: SubstitutionBuilder = SubstitutionBuilder()
    addSubstitutions
    (for
      workingDir = testDir(testName)
      _ = scribe.info(s"Working directory: $workingDir")
      _ <- resolveTemplates(testName, workingDir, builder.all)
      _ <- compile(workingDir)
      out <- run(workingDir)
      _ = scribe.info(s"Test output: $out")
    yield out).fold(err => fail(s"Test failed: ${err.getMessage}"), out => compare(out, testName))

  def testDir(directoryName: String): Path = Files.createTempDirectory(directoryName)

  def resolveTemplates(testName: String, workingDir: Path, substitutions: Set[(Pattern, String)]): Try[Unit] = Try:
    templatePaths.foreach(template => copy(template, workingDir.resolve(baseName(template))))
    val templateContent = read(programTemplatePath)
    val programContent = read(programContentPath(testName))
    val updatedContent = substitutions.foldLeft(templateContent.appendedAll(programContent)):
      case (content, (pattern, replacement)) =>
        val newContent = content.replace(pattern, replacement)
        if newContent == content then throw IllegalStateException(s"No pattern ${pattern} found in content.")
        else newContent
    write(workingDir.resolve(mainFileName), updatedContent)

  def programContentPath(testName: String): Path

  def programTemplatePath: Path

  def compile(workingDir: Path): Try[Unit]

  def run(workingDir: Path): Try[String]

  def compare(actual: String, testName: String): Assertion =
    val expected = Try(read(snapshotsFolderPath.resolve(testName)))
    scribe.info(s"Expected snapshot for test $testName: ${expected.getOrElse("Not found")}")
    if expected.isFailure then
      scribe.error(s"Missing snapshot for test $testName")
      fail(s"Missing snapshot for test $testName")
    else expected.get.trim shouldBe actual.trim

end PlatformTest

trait FileSystem:

  def copy(origin: Path, destination: Path): Unit = Files.copy(origin, destination): Unit

  def baseName(path: Path): String = path.getFileName().toString.replaceFirst("\\.[^.]+$", "")

  def read(path: Path): String = Files.readString(path, StandardCharsets.UTF_8)

  def write(path: Path, content: String): Unit = Files.writeString(
    path,
    content,
    StandardCharsets.UTF_8,
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
  ): Unit

object PlatformTest:
  type Pattern = String :| (StartWith["{{"] & EndWith["}}"])

  class SubstitutionBuilder:
    private var substitutions = Set.empty[(Pattern, String)]
    private[PlatformTest] def add(key: Pattern, value: String): Unit = substitutions += ((key, value))
    def all: Set[(Pattern, String)] = substitutions.view.toSet

  extension (pattern: Pattern)
    infix inline def ->(value: String)(using builder: SubstitutionBuilder): Unit = builder.add(pattern, value)
