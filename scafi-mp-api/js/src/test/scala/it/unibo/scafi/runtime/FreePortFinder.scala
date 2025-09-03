package it.unibo.scafi.runtime

import scala.concurrent.{ Future, Promise }

import it.unibo.scafi.runtime.network.sockets.InetTypes.Port
import it.unibo.scafi.runtime.network.sockets.Net

import io.github.iltotore.iron.refineUnsafe

object FreePortFinder:

  def findFreePorts(n: Int): Future[Set[Port]] =
    val ports = scala.collection.mutable.Set.empty[Port]
    val promise = Promise[Set[Port]]()
    def next(count: Int): Unit = count match
      case `n` => promise.success(ports.toSet)
      case _ =>
        val server = Net.createServer(_ => ())
        server.listen(0): () =>
          ports.add(server.address().port.refineUnsafe): Unit
          server.close()
          next(ports.size)
        server.onError(err => promise.failure(Exception(err.message)))
    next(0)
    promise.future
