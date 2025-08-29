package it.unibo.scafi.libraries

import it.unibo.scafi

import scafi.message.Codable
import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager

trait PortableRuntime:
  self: PortableTypes =>

  given [Value, Format]: Codable[Value, Format] = compiletime.deferred

  trait ADTs:

    @JSExport
    case class Endpoint(address: String, port: Int)

  trait Interface:
    self: ADTs =>

    @JSExport
    def socketNetwork[ID](deviceId: ID, port: Int, neighbors: Map[ID, Endpoint]): ConnectionOrientedNetworkManager[ID]
