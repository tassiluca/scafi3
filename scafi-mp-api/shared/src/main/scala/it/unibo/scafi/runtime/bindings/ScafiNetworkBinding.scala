package it.unibo.scafi.runtime.bindings

import scala.concurrent.ExecutionContext

import it.unibo.scafi

import io.github.iltotore.iron.refineUnsafe

import scafi.runtime.PortableRuntime
import scafi.libraries.PortableTypes
import scafi.runtime.network.sockets.{ ConnectionConfiguration, ConnectionOrientedNetworkManager, SocketNetworkManager }
import it.unibo.scafi.runtime.network.sockets.InetTypes

/**
 * Provides a concrete implementation of the portable runtime API for the ScaFi network.
 */
trait ScafiNetworkBinding extends PortableRuntime:
  self: PortableTypes =>

  trait NetworkBindings(using ExecutionContext) extends Api:
    self: Requirements & Adts =>

    override def socketNetwork[ID](
        deviceId: ID,
        port: Int,
        neighbors: Map[ID, Endpoint],
    ): ConnectionOrientedNetworkManager[DeviceId] =
      val net = neighbors.view
        .map((id, e) => (id, toInetEndpoint(e)))
        .toMap
        .asInstanceOf[collection.immutable.Map[DeviceId, InetTypes.Endpoint]]
      SocketNetworkManager.withFixedNeighbors(deviceId, port.refineUnsafe, net)

    given ConnectionConfiguration = ConnectionConfiguration.basic
