package it.unibo.scafi.libraries

import java.util.concurrent.Executors

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.Future.sequence

import it.unibo.scafi.integration.CTests
import it.unibo.scafi.integration.PlatformTest.ProgramOutput
import it.unibo.scafi.test.environment.Grids.vonNeumannGrid
import it.unibo.scafi.utils.FreePortFinder

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.{ Seconds, Span }

class CNativeTests extends AnyFlatSpec with CTests with ScalaFutures:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  given PatienceConfig = PatienceConfig(timeout = Span(180, Seconds))

  "Neighbors discovery program" should "spread local values to neighborhood" in:
    sequence:
      cAggregateResult("neighbors-discovery", rows = 2, cols = 2)
    .futureValue should contain theSameElementsAs Seq(
      0 -> cField(default = 0, neighbors = Map(1 -> 1, 2 -> 2)),
      1 -> cField(default = 1, neighbors = Map(0 -> 0, 3 -> 3)),
      2 -> cField(default = 2, neighbors = Map(0 -> 0, 3 -> 3)),
      3 -> cField(default = 3, neighbors = Map(1 -> 1, 2 -> 2)),
    )

  private def cAggregateResult(testName: String, rows: Int, cols: Int): Seq[Future[(Int, ProgramOutput)]] =
    val ports = FreePortFinder.get(rows * cols)
    vonNeumannGrid(rows, cols): (id, neighbors) =>
      for
        neighborsAsCEntries = neighbors
          .map(nid => s"""
               |BinaryCodable neighbor_$nid = int_codable_of($nid);
               |struct Endpoint device_endpoint_$nid = { "localhost", ${ports(nid)} };
               |Neighborhood_put(neighbors, &neighbor_$nid, &device_endpoint_$nid);
               |""".stripMargin)
          .mkString(sep = "\n")
        res <- Future:
          testProgram(testName):
            "{{ deviceId }}" -> id.toString
            "{{ port }}" -> ports(id).toString
            "{{ neighbors }}" -> neighborsAsCEntries
      yield id -> res.getOrElse(fail(s"C test '$testName' failed on device '$id': ${res.failed.get}"))

  private def cField[V <: Any](default: V, neighbors: Map[Int, V]): String =
    s"Field($default, ${neighbors.mkString("[", ", ", "]")})"
end CNativeTests
