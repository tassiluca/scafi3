package it.unibo.scafi.mp.api

import it.unibo.scafi.mp.api.test.JSPlatformTest
import org.scalatest.flatspec.AnyFlatSpec
import it.unibo.scafi.runtime.network.sockets.InetTypes.Port
import scala.concurrent.Future
import it.unibo.scafi.mp.api.test.SimpleGrids.vonNeumannGrid
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class JSNativeTests extends AnyFlatSpec with JSPlatformTest:

  given ExecutionContext = ExecutionContext.global

  "First joke" should "be funny" in:
    val portsPool = Seq[Port](5050, 5051)
    scribe.info(s"Allocated ports: ${portsPool.mkString(", ")}")
    val results = Future.sequence:
      vonNeumannGrid(rows = 2, cols = 1): (id, neighbors) =>
        val neighborsAsJsEntries = neighbors
          .map(nid => s"[$nid, Runtime.Endpoint('localhost', ${portsPool(nid)})]")
          .mkString("[", ", ", "]")
        Future:
          testProgram("simple-exchange"):
            "{{ deviceId }}" -> id.toString
            "{{ port }}" -> portsPool(id).toString
            "{{ neighbors }}" -> neighborsAsJsEntries
    Await.result(results, 15.seconds)
