package it.unibo.scafi.runtime

import it.unibo.scafi

import scafi.context.xc.ExchangeAggregateContext
import scafi.libraries.PortableTypes
import scafi.message.UniversalCodable
import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager

/**
 * Portable runtime API entry point.
 */
trait PortableRuntime:
  self: PortableTypes =>

  trait Adts:

    /** The type used internally to the scafi core engine to identify devices. TODO: comparable? */
    type DeviceId

    /** Generic type parameters can be turned into internal `DeviceId` representation and viceversa. */
    given deviceIdIso[ID]: Iso[DeviceId, ID] = compiletime.deferred

    /** A network endpoint consisting of an [[address]] and a [[port]]. */
    type Endpoint

    /** Endpoint is isomorphic to [[scafi.runtime.network.sockets.InetTypes.Endpoint]]. */
    given toInetEndpoint: Conversion[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] = compiletime.deferred

  /** The requirements that must be satisfied by any concrete implementation of the runtime. */
  trait Requirements extends MemorySafeContext:
    self: Adts =>

    /** The universal codable instance used for encoding and decoding values to be sent over the network. */
    given deviceIdCodable[Format]: UniversalCodable[DeviceId, Format] = compiletime.deferred

    /** The aggregate library entry point. */
    type AggregateLibrary

    /** @return the concrete portable aggregate library instance. */
    def library[ID](using Arena): ExchangeAggregateContext[ID] ?=> AggregateLibrary

  /** The portable runtime API. */
  trait Api:
    self: Requirements & Adts =>

    /** @return a socket-based network working on [[port]] with statically assigned [[neighbors]] and [[deviceId]]. */
    @JSExport
    def socketNetwork[ID](
        deviceId: ID,
        port: Int,
        neighbors: Map[ID, Endpoint],
    ): ConnectionOrientedNetworkManager[DeviceId]

    /**
     * Runs the given aggregate [[program]] on the device with the given [[deviceId]], using the provided [[network]].
     * The result of the program is passed to the [[onResult]] callback, which should return an [[Outcome]] indicating
     * whether the execution needs to continue or stop.
     */
    @JSExport
    def engine[ID, Result](
        deviceId: ID,
        network: ConnectionOrientedNetworkManager[DeviceId],
        program: Function1[AggregateLibrary, Result],
        onResult: Function1[Result, Outcome[Boolean]],
    ): Outcome[Unit]
  end Api
end PortableRuntime
