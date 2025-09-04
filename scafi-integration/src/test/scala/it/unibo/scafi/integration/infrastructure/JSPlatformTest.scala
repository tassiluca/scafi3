package it.unibo.scafi.mp.api.test

import java.nio.file.Path

import scala.util.{ Success, Try }

trait JSPlatformTest extends PlatformTest:

  private val packageJsonPath = resource("js/package.json")

  override val templatePaths: Set[Path] = Set(resource("js/main.template.mjs"), packageJsonPath)

  override val snapshotsFolderPath: Path = resource("snapshots")

  override def programUnderTest(testName: String): Path = resource(s"js/$testName/program.mjs")

  override def compile(workingDir: Path): Try[Unit] = Success(())

  override def run(workingDir: Path): Try[String] = Try:
    val processBuilder = new ProcessBuilder("node", "main.mjs")
    processBuilder.environment.put("SCAFI3", s"${rootProjectPath}/scafi-mp-api/js/target/fullLinkJS/main.mjs")
    processBuilder.directory(workingDir.toFile)
    val process = processBuilder.start()
    val exitCode = process.waitFor()
    if exitCode != 0 then
      val errorStream = scala.io.Source.fromInputStream(process.getErrorStream).mkString
      throw new RuntimeException(s"Process exited with code $exitCode. Error: $errorStream")
    else scala.io.Source.fromInputStream(process.getInputStream).mkString

  private val rootProjectPath: String = System.getProperty("user.dir")
end JSPlatformTest
