package it.unibo.scafi.runtime

import scala.util.{ Success, Try }

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import io.github.iltotore.iron.autoRefine

import scalajs.js

class JSPlatformTest extends AnyFlatSpec with PlatformTest with should.Matchers:

  val rootProjectPath = NodeProcess.cwd()
  override val snapshotsFolderPath: Path = s"$rootProjectPath/scafi-mp-api/js/src/test/resources/snapshots/js"
  val testResourcesPath = s"$rootProjectPath/scafi-mp-api/js/src/test/resources/js"
  override val programTemplatePath = s"$testResourcesPath/program.template.mjs"
  val packageFilePath = s"$testResourcesPath/package.json"
  override val templatePaths: Set[Path] = Set(programTemplatePath, packageFilePath)

  it should "work" in:
    test("simple-exchange"):
      "{{deviceId}}" -> "10"
      "{{port}}" -> "100"

  override def testDir(directoryName: String): Path =
    val osTmpDir = OS.tmpdir()
    val tempDir = s"$osTmpDir/$directoryName"
    FS.rmSync(tempDir, FSOptions.recursiveForce)
    FS.mkdirSync(tempDir)
    scribe.info(s"Temporary directory created at: $tempDir")
    tempDir

  override def read(path: Path): String = FS.readFileSync(path, "utf-8")

  override def write(path: Path, content: String): Unit = FS.writeFileSync(path, content)

  override def copy(origin: Path, destination: Path): Unit = FS.copyFileSync(origin, destination)

  override def baseName(path: Path): String = Path.basename(path)

  override def programContentPath(testName: String): Path = s"$testResourcesPath/$testName/program.mjs"

  override def compile(workingDir: Path): Try[Unit] = Success(())

  override def run(workingDir: Path): Try[String] = Try:
    ChildProcess.execSync("node main.mjs", js.Dictionary(("cwd", workingDir))).toString()
end JSPlatformTest
