package it.unibo.scafi.mp.api

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.DurationInt

import it.unibo.scafi.mp.api.test.JSPlatformTest
import it.unibo.scafi.mp.api.test.SimpleGrids.vonNeumannGrid
import it.unibo.scafi.runtime.FreePortFinder

import org.scalatest.flatspec.AnyFlatSpec

class JSNativeTests extends AnyFlatSpec with JSPlatformTest:

  given ExecutionContext = ExecutionContext.global

  "First joke" should "be funny" in:
    val portsPool = FreePortFinder.get(2)
    scribe.info(s"Allocated ports: ${portsPool.mkString(", ")}")
    val results = Future.sequence:
      vonNeumannGrid(rows = 2, cols = 1): (id, neighbors) =>
        val neighborsAsJsEntries = neighbors
          .map(nid => s"[$nid, Runtime.Endpoint('localhost', ${portsPool(nid)})]")
          .mkString("[", ", ", "]")
        Future:
          testProgram("simple-exchange", id):
            "{{ deviceId }}" -> id.toString
            "{{ port }}" -> portsPool(id).toString
            "{{ neighbors }}" -> neighborsAsJsEntries
    Await.result(results, 15.seconds)
