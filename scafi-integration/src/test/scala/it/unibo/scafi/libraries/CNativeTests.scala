package it.unibo.scafi.libraries

import java.util.concurrent.Executors

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.Future.sequence

import it.unibo.scafi.integration.CTests
import it.unibo.scafi.integration.PlatformTest.ProgramOutput
import it.unibo.scafi.test.environment.Grids.vonNeumannGrid

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.{ Seconds, Span }

class CNativeTests extends AnyFlatSpec with CTests with ScalaFutures:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  given PatienceConfig = PatienceConfig(timeout = Span(180, Seconds))

  "Simple test" should "pass" in:
    sequence:
      cAggregateResult("simple", rows = 2, cols = 2)
    .futureValue should contain theSameElementsAs Seq(
      0 -> "Hello World from Scafi3 tests",
      1 -> "Hello World from Scafi3 tests",
      2 -> "Hello World from Scafi3 tests",
      3 -> "Hello World from Scafi3 tests",
    )

  private def cAggregateResult(testName: String, rows: Int, cols: Int): Seq[Future[(Int, ProgramOutput)]] =
    // val ports = FreePortFinder.get(rows * cols)
    vonNeumannGrid(rows, cols): (id, _) =>
      // neighborsAsJsEntries = neighbors
      //   .map(nid => s"[$nid, Runtime.Endpoint('localhost', ${ports(nid)})]")
      //   .mkString("[", ", ", "]")
      Future:
        testProgram(testName):
          "{{ message }}" -> "Hello World from Scafi3 tests"
            // "{{ deviceId }}" -> id.toString
            // "{{ port }}" -> ports(id).toString
            // "{{ neighbors }}" -> neighborsAsJsEntries
      .map: res =>
        id -> res.getOrElse(fail(s"C test '$testName' failed on device '$id': ${res.failed.get.getMessage}"))
end CNativeTests
