package it.unibo.scafi.mp.api.test

import java.nio.file.Path

import scala.io.Source
import scala.util.{ Success, Try, Using }
import scala.util.chaining.scalaUtilChainingOps

trait JSPlatformTest extends PlatformTest:

  override val templatePaths: Set[Path] = Set(resource("js/main.template.mjs"), resource("js/package.json"))

  override def programUnderTest(testName: String): Path = resource(s"js/$testName/program.mjs")

  override def compile(workingDir: Path): Try[Unit] = Success(())

  override def run(workingDir: Path): Try[String] = Try:
    val process = new ProcessBuilder("npm", "start", "--silent")
      .directory(workingDir.toFile)
      .tap(_.environment.put("SCAFI3", scafiJsBundlePath))
      .start()
    process.waitFor() match
      case 0 => Using.resource(Source.fromInputStream(process.getInputStream))(_.mkString)
      case _ =>
        val err = Using.resource(Source.fromInputStream(process.getErrorStream))(_.mkString)
        throw new RuntimeException(s"Node process failed: $err")

  private val scafiJsBundlePath = s"${System.getProperty("user.dir")}/scafi-mp-api/js/target/fullLinkJS/main.mjs"

end JSPlatformTest
