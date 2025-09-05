package it.unibo.scafi.runtime

import it.unibo.scafi.runtime.network.sockets.InetTypes.Port

import io.github.iltotore.iron.refineUnsafe
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

object FreePortFinder:
  import java.net.ServerSocket

  /** @return a pool of free ports on the local machine with the requested [[size]]. */
  def get(size: Int): Seq[Port] =
    val sockets = (1 to size).map(_ => new ServerSocket(0))
    try
      sockets.map: s =>
        s.setReuseAddress(true) // allow the port to be reused immediately after closing
        s.getLocalPort().refineUnsafe
    finally sockets.foreach(_.close())

class FreePortFinderTest extends AnyFlatSpec with should.Matchers:

  "FreePortFinder" should "return the requested number of free ports" in:
    val freePorts = FreePortFinder.get(5)
    println(freePorts)
    freePorts.size shouldBe 5
