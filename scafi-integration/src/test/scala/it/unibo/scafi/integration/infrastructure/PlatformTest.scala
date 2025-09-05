package it.unibo.scafi.mp.api.test

import java.nio.file.Path

import scala.util.Try

import it.unibo.scafi.integration.infrastructure.FileSystem
import it.unibo.scafi.mp.api.test.PlatformTest.{ Pattern, Substitution, SubstitutionBuilder }

import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.string.{ EndWith, StartWith }
import org.scalatest.matchers.should

trait PlatformTest extends should.Matchers with FileSystem:
  export PlatformTest.->
  export io.github.iltotore.iron.autoRefine

  /** The set of template files from which bootstrapping the test working directory. */
  val templatePaths: Set[Path]

  def testProgram(testName: String)(addSubstitutions: SubstitutionBuilder ?=> Unit): Try[String] =
    given builder: SubstitutionBuilder = SubstitutionBuilder()
    addSubstitutions
    for
      workingDir = createTempDirectory(testName)
      _ = scribe.info(s"Working directory: $workingDir")
      _ <- resolveTemplates(testName, workingDir, builder.all)
      _ <- compile(workingDir)
      out <- run(workingDir)
    yield out

  private def resolveTemplates(testName: String, workingDir: Path, substitutions: Set[Substitution]): Try[Unit] = Try:
    templatePaths.foreach(template => copy(template, workingDir.resolve(template.getFileName)))
    val templateFile = templatePaths
      .find(_.getFileName.toString.contains("template"))
      .getOrElse(throw IllegalStateException("No template file found!"))
    val templateContent = read(templateFile)
    val programUnderTestContent = read(programUnderTest(testName))
    val updatedContent = substitutions.foldLeft(templateContent.appendedAll(programUnderTestContent)):
      case (content, (pattern, substitution)) =>
        val newContent = content.replace(pattern, substitution)
        if newContent == content then throw IllegalStateException(s"No pattern ${pattern}!")
        else newContent
    write(workingDir.resolve(templateFile.getFileName.toString.replace(".template", "")), updatedContent)

  def programUnderTest(testName: String): Path

  /** Compile the program in the given [[workingDir]]. */
  def compile(workingDir: Path): Try[Unit]

  /** Run the program in the given [[workingDir]] and return its output. */
  def run(workingDir: Path): Try[String]

  protected def resource(name: String): Path = Path.of(getClass.getClassLoader.getResource(name).getPath())

end PlatformTest

object PlatformTest:

  /** A pattern is a portion of text to be replaced in a template file in the form of `{{ var }}`. */
  type Pattern = String :| (StartWith["{{"] & EndWith["}}"])

  /** A substitution is a mapping from a pattern to a replacement string. */
  type Substitution = (Pattern, String)

  class SubstitutionBuilder:
    private var substitutions = Set.empty[Substitution]
    private[PlatformTest] def add(key: Pattern, value: String): Unit = substitutions += ((key, value))
    def all: Set[Substitution] = substitutions.view.toSet

  extension (pattern: Pattern)
    /** Substitutes the pattern with the given value in a template file. */
    infix inline def ->(value: String)(using builder: SubstitutionBuilder): Unit = builder.add(pattern, value)
