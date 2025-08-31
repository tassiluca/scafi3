package it.unibo.scafi.libraries.bindings

import scala.concurrent.ExecutionContext

import it.unibo.scafi

import io.github.iltotore.iron.refineUnsafe

import scafi.libraries.{ PortableRuntime, PortableTypes }
import scafi.runtime.network.sockets.{ ConnectionConfiguration, ConnectionOrientedNetworkManager, SocketNetworkManager }

trait ScafiNetworkBinding extends PortableRuntime:
  self: PortableTypes =>

  given ExecutionContext = compiletime.deferred

  given ConnectionConfiguration = ConnectionConfiguration.basic

  trait NetworkBindings extends Interface:
    self: ADTs =>

    override def socketNetwork[ID](
        deviceId: ID,
        port: Int,
        neighbors: Map[ID, Endpoint],
    ): ConnectionOrientedNetworkManager[ID] = SocketNetworkManager
      .withFixedNeighbors(deviceId, port.refineUnsafe, neighbors.view.mapValues(endpointIso.get).toMap)

    given endpointIso: Iso[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] = Iso((e: Endpoint) =>
      scafi.runtime.network.sockets.InetTypes.Endpoint(e.address.refineUnsafe, e.port.refineUnsafe),
    )(e => Endpoint(e.address, e.port))
end ScafiNetworkBinding
