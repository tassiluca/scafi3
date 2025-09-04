package it.unibo.scafi.mp.api.test

import java.nio.file.Path

import scala.util.{ Success, Try }

trait JSPlatformTest extends PlatformTest:

  override val mainFileName: String = "main.mjs"

  private val packageJsonPath = Path.of(getClass.getClassLoader.getResource("js/package.json").getPath())
  override val templatePaths: Set[Path] = Set(programTemplatePath, packageJsonPath)

  override val snapshotsFolderPath: Path = Path.of(getClass.getClassLoader.getResource("snapshots").getPath())

  override def programContentPath(testName: String): Path =
    Path.of(getClass.getClassLoader.getResource(s"js/$testName/program.mjs").getPath())

  override def programTemplatePath: Path =
    Path.of(getClass.getClassLoader.getResource("js/program.template.mjs").getPath())

  override def compile(workingDir: Path): Try[Unit] = Success(())

  override def run(workingDir: Path): Try[String] = Try:
    val processBuilder = new ProcessBuilder("node", mainFileName)
    val envs = processBuilder.environment()
    val rootProjectPath = System.getProperty("user.dir")
    envs.put("SCAFI3", s"${rootProjectPath}/scafi-mp-api/js/target/fastLinkJS/main.mjs")
    processBuilder.directory(workingDir.toFile)
    val process = processBuilder.start()
    val exitCode = process.waitFor()
    if exitCode != 0 then
      val errorStream = scala.io.Source.fromInputStream(process.getErrorStream).mkString
      throw new RuntimeException(s"Process exited with code $exitCode. Error: $errorStream")
    else scala.io.Source.fromInputStream(process.getInputStream).mkString
end JSPlatformTest
