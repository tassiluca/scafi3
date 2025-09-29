package it.unibo.scafi.integration

import java.nio.file.Path

import scala.io.Source
import scala.util.{ Try, Using }

import it.unibo.scafi.integration.PlatformTest

import cats.syntax.all.catsSyntaxTuple3Semigroupal

trait CTests extends PlatformTest:

  override def programUnderTest(testName: String): Try[Path] = resource(s"c/$testName/program.c")

  override def templates(testName: String): Try[Set[Path]] = (
    resource(s"c/$testName").flatMap(findAll),
    resource("c/main.template.c"),
    resource("c/Makefile"),
  ).mapN(_ + _ + _)

  override def compile(workingDir: Path): Try[Unit] = Try:
    val process = new ProcessBuilder("make")
      .directory(workingDir.toFile)
      .start()
    process.waitFor() match
      case 0 => ()
      case _ =>
        val err = Using.resource(Source.fromInputStream(process.getErrorStream))(_.mkString)
        throw new RuntimeException(s"Make process failed: $err")

  override def run(workingDir: Path): Try[String] = Try:
    val mainCommand = workingDir.resolve(if isWindows then "main.exe" else "main")
    val process = new ProcessBuilder(mainCommand.toString)
      .directory(workingDir.toFile)
      // .tap(_.environment.put("SCAFI3", scafiJsBundlePath))
      .start()
    process.waitFor() match
      case 0 => Using.resource(Source.fromInputStream(process.getInputStream))(_.mkString)
      case _ =>
        val err = Using.resource(Source.fromInputStream(process.getErrorStream))(_.mkString)
        throw new RuntimeException(s"Node process failed: $err")

  // private def scafiJsBundlePath = Paths
  //   .get(System.getProperty("user.dir"), "scafi-mp-api", "js", "target", "fullLinkJS", "main.mjs")
  //   .toAbsolutePath
  //   .toUri
  //   .toString
end CTests
