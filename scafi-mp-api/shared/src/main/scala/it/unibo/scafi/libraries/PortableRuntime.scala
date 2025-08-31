package it.unibo.scafi.libraries

import it.unibo.scafi

import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import it.unibo.scafi.message.RegisterableCodable

trait PortableRuntime:
  self: PortableTypes =>

  given [Value, Format]: RegisterableCodable[Value, Format] = compiletime.deferred

  trait ADTs:

    @JSExport
    case class Endpoint(address: String, port: Int)

  trait Interface:
    self: ADTs =>

    @JSExport
    def socketNetwork[ID](deviceId: ID, port: Int, neighbors: Map[ID, Endpoint]): ConnectionOrientedNetworkManager[ID]
