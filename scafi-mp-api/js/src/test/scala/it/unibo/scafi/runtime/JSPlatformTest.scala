package it.unibo.scafi.runtime

import scalajs.js

import scala.util.Try
import scala.util.Success
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class JSPlatformTest extends AnyFlatSpec with PlatformTest with should.Matchers:

  val rootProjectPath = NodeProcess.cwd()
  val testSnapshotsPath = s"$rootProjectPath/scafi-mp-api/js/src/test/resources/snapshots/js"
  val testResourcesPath = s"$rootProjectPath/scafi-mp-api/js/src/test/resources/js"
  val templateFilePath = s"$testResourcesPath/program.template.mjs"
  val packageFilePath = s"$testResourcesPath/package.json"

  it should "work" in:
    test("js-first-test")

  override def testDir(directoryName: String): Path =
    val osTmpDir = OS.tmpdir()
    val tempDir = s"$osTmpDir/$directoryName"
    FS.rmSync(tempDir, FSOptions.recursiveForce)
    FS.mkdirSync(tempDir)
    scribe.info(s"Temporary directory created at: $tempDir")
    tempDir

  override def resolveTemplates(testName: String, workingDir: Path): Unit =
    freshProjectCopy(workingDir)
    val templateFile = s"$workingDir/program.template.mjs"
    val content = FS.readFileSync(templateFile, "utf-8")
    val updatedContent = content.replace("{{deviceId}}", "test-device-id")
    FS.writeFileSync(s"$workingDir/main.mjs", updatedContent)

  private def freshProjectCopy(workingDir: Path): Unit =
    val resources = List(templateFilePath, packageFilePath)
    resources.foreach: file =>
      val target = s"$workingDir/${Path.basename(file)}"
      FS.copyFileSync(file, target)

  override def compile(workingDir: Path): Try[Unit] = Success(())

  override def run(workingDir: Path): Try[String] = Try:
    ChildProcess.execSync("node main.mjs", js.Dictionary("cwd" -> workingDir)).toString()
end JSPlatformTest
