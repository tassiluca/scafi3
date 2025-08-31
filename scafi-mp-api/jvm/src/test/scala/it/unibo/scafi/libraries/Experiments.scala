package it.unibo.scafi.libraries

import java.nio.charset.StandardCharsets

import scala.concurrent.ExecutionContext

import it.unibo.scafi.context.AggregateContext
import it.unibo.scafi.context.xc.ExchangeAggregateContext.exchangeContextFactory
import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.runtime.ScafiEngine
import it.unibo.scafi.runtime.network.sockets.SocketNetworkManager
import it.unibo.scafi.runtime.network.sockets.InetTypes.{ Endpoint, Localhost }
import it.unibo.scafi.message.BinaryCodable
import it.unibo.scafi.runtime.network.sockets.ConnectionConfiguration
import it.unibo.scafi.runtime.network.sockets.InetTypes.Port
import it.unibo.scafi.libraries.All.*

import io.github.iltotore.iron.autoRefine
import it.unibo.scafi.language.common.syntax.BranchingSyntax

object Experiments:

  type ID = Int

  type Lang = AggregateContext { type DeviceId = ID } & AggregateFoundation & FieldBasedSharedData & ExchangeSyntax &
    BranchingSyntax

  given ExecutionContext = ExecutionContext.global

  given ConnectionConfiguration = ConnectionConfiguration.basic

  given BinaryCodable[ID] = new BinaryCodable[ID]:
    def encode(msg: ID): Array[Byte] = msg.toString.getBytes(StandardCharsets.UTF_8)
    def decode(bytes: Array[Byte]): ID = new String(bytes, StandardCharsets.UTF_8).toInt

  case class Neighbor[ID](id: ID, port: Port)

  @main def node1(): Unit =
    val neighbor = Neighbor(3, 5053)
    val me = Neighbor(1, 5051)
    val net =
      SocketNetworkManager.withFixedNeighbors(me.id, me.port, Map(neighbor.id -> Endpoint(Localhost, neighbor.port)))

    def program(using Lang) =
      branch(localId % 2 == 0)(
        exchange(1)(returnSending),
        exchange(0)(returnSending),
      )

    net.start().onComplete(res => println(s"Node ${me.id} network started: $res"))

    val engine = ScafiEngine(me.id, net, exchangeContextFactory)(program)

    while true do
      val result = engine.cycle()
      println(result)
      Thread.sleep(1000)
  end node1

  @main def node2(): Unit =
    val neighbor = Neighbor(1, 5051)
    val me = Neighbor(3, 5053)
    val net =
      SocketNetworkManager.withFixedNeighbors(me.id, me.port, Map(neighbor.id -> Endpoint(Localhost, neighbor.port)))

    net.start().onComplete(res => println(s"Node ${me.id} network started: $res"))

    def program(using Lang) =
      branch(localId % 2 == 0)(
        exchange(1)(returnSending),
        exchange(0)(returnSending),
      )

    val engine = ScafiEngine(me.id, net, exchangeContextFactory)(program)

    while true do
      val result = engine.cycle()
      println(result)
      Thread.sleep(1000)
  end node2
end Experiments
