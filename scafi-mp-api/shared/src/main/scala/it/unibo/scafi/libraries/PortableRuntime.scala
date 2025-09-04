package it.unibo.scafi.libraries

import it.unibo.scafi
import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.message.RegisterableCodable

import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager

trait PortableRuntime[AggregateLibrary]:
  self: PortableTypes =>

  // Requirements?

  given [Value, Format]: RegisterableCodable[Value, Format] = compiletime.deferred

  def library[ID](using ctx: ExchangeAggregateContext[ID]): AggregateLibrary

  trait ADTs:

    @JSExport @JSExportAll
    case class Endpoint(address: String, port: Int)

  trait Interface:
    self: ADTs =>

    @JSExport
    def socketNetwork[ID](deviceId: ID, port: Int, neighbors: Map[ID, Endpoint]): ConnectionOrientedNetworkManager[ID]

    @JSExport
    def engine[ID, Result](
        deviceId: ID,
        network: ConnectionOrientedNetworkManager[ID],
        program: Function1[AggregateLibrary, Result],
        onResult: Function1[Result, Handler[Boolean]],
    ): Unit
end PortableRuntime
