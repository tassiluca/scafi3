package it.unibo.scafi.runtime.bindings

import scala.concurrent.ExecutionContext

import it.unibo.scafi

import io.github.iltotore.iron.refineUnsafe

import scafi.runtime.PortableRuntime
import scafi.libraries.PortableTypes
import scafi.runtime.network.sockets.{ ConnectionConfiguration, ConnectionOrientedNetworkManager, SocketNetworkManager }
import it.unibo.scafi.runtime.network.sockets.InetTypes.Port

/**
 * Provides a concrete implementation of the portable runtime API for the ScaFi network.
 */
trait ScafiNetworkBinding extends PortableRuntime:
  self: PortableTypes =>

  trait NetworkBindings(using ExecutionContext) extends Api:
    self: Requirements =>

    override def socketNetwork[ID](
        deviceId: ID,
        port: Int,
        neighbors: Map[ID, Endpoint],
    ): ConnectionOrientedNetworkManager[ID] =
      println(">>>> [network] PORT: " + port)
      val refinedPort: Port = port.refineUnsafe
      println(">>>> [network] port refined " + (refinedPort: Port))
      SocketNetworkManager.withFixedNeighbors(
        deviceId,
        refinedPort,
        neighbors.view.mapValues(endpointIso.get).toMap,
      )

    given ConnectionConfiguration = ConnectionConfiguration.basic

    given endpointIso: Iso[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] = Iso((e: Endpoint) =>
      scafi.runtime.network.sockets.InetTypes.Endpoint(e.address.refineUnsafe, e.port.refineUnsafe),
    )(e => Endpoint(e.address, e.port))
  end NetworkBindings
end ScafiNetworkBinding
