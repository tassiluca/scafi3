package it.unibo.scafi.runtime

import scala.concurrent.ExecutionContext

import it.unibo.scafi.test.AsyncSpec

class FreePortFinderTest extends AsyncSpec:

  override given executionContext: ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

  "findFreePorts" should "return the requested number of free ports" in:
    FreePortFinder.findFreePorts(5) verifying: ports =>
      eventually:
        println(ports)
        ports.size shouldBe 5
