package it.unibo.scafi.mp.api

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.Future.sequence
import scala.util.{ Failure, Success, Try }

import it.unibo.scafi.libraries.BranchingLibraryTest.{ hasSameParityAs, isEven }
import it.unibo.scafi.mp.api.test.JSPlatformTest
import it.unibo.scafi.mp.api.test.SimpleGrids.vonNeumannGrid
import it.unibo.scafi.runtime.FreePortFinder
import it.unibo.scafi.runtime.network.sockets.InetTypes.Port

import org.scalatest.Assertion
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.time.{ Seconds, Span }

class JSNativeTests extends AnyFlatSpec with JSPlatformTest with ScalaFutures:

  given ExecutionContext = ExecutionContext.global

  given PatienceConfig = PatienceConfig(timeout = Span(10, Seconds))

  "Neighbors discovery program" should "spread local values to neighborhood" in:
    val ports = FreePortFinder.get(4)
    val results = vonNeumannGrid(rows = 2, cols = 2): (id, neighbors) =>
      val expectedNeighbors = neighbors.map(n => n -> n).toMap
      Future(jsAggregateResult("neighbors-discovery", ports, id, neighbors)) shouldBe s"Field($id, $expectedNeighbors)"
    sequence(results).futureValue

  "Exchange aggregate program with branch restriction" should "correctly spread local values to aligned neighbors" in:
    val ports = FreePortFinder.get(4)
    val results = vonNeumannGrid(rows = 2, cols = 2): (id, neighbors) =>
      val expectedNeighbors = neighbors
        .filter(_.hasSameParityAs(id))
        .map(n => n -> (if n.isEven then true else false))
        .toMap
      Future(
        jsAggregateResult("restricted-exchange", ports, id, neighbors),
      ) shouldBe s"Field(${id.isEven}, $expectedNeighbors)"
    sequence(results).futureValue

  extension (result: Future[Try[String]])
    infix def shouldBe(expected: String): Future[Assertion] = result.map:
      case Success(out) =>
        println(s"Got output: $out")
        out shouldBe expected
      case Failure(err) => fail(err.getMessage)

  private def jsAggregateResult(testName: String, portsPool: Seq[Port], id: Int, neighbors: Seq[Int]) =
    val neighborsAsJsEntries = neighbors
      .map(nid => s"[$nid, Runtime.Endpoint('localhost', ${portsPool(nid)})]")
      .mkString("[", ", ", "]")
    testProgram(testName):
      "{{ deviceId }}" -> id.toString
      "{{ port }}" -> portsPool(id).toString
      "{{ neighbors }}" -> neighborsAsJsEntries
end JSNativeTests
