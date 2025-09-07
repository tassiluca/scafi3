package it.unibo.scafi.runtime

import it.unibo.scafi
import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.libraries.PortableTypes
import it.unibo.scafi.message.RegisterableCodable

import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager

trait PortableRuntime:
  self: PortableTypes =>

  trait Requirements:

    given [Value, Format]: RegisterableCodable[Value, Format] = compiletime.deferred

    type AggregateLibrary

    def library[ID]: ExchangeAggregateContext[ID] ?=> AggregateLibrary

  trait Adts:

    @JSExport @JSExportAll
    case class Endpoint(address: String, port: Int)

  trait Api extends Adts:
    self: Requirements =>

    @JSExport
    def socketNetwork[ID](deviceId: ID, port: Int, neighbors: Map[ID, Endpoint]): ConnectionOrientedNetworkManager[ID]

    @JSExport
    def engine[ID, Result](
        deviceId: ID,
        network: ConnectionOrientedNetworkManager[ID],
        program: Function1[AggregateLibrary, Result],
        onResult: Function1[Result, Handler[Boolean]],
    ): Handler[Unit]
end PortableRuntime
