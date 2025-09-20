package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.exported

import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.libraries.FullLibrary
import it.unibo.scafi.message.UniversalCodable
import it.unibo.scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import it.unibo.scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import it.unibo.scafi.types.NativeTypes

object NativeScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeRequirements extends Requirements:
    type AggregateLibrary = FullLibrary

    override given [Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
      override def encode(value: Value): Format = ???
      override def decode(data: Format): Value = ???
      override def register(value: Value): Unit = ???

    override def library[ID]: ExchangeAggregateContext[ID] ?=> FullLibrary = FullLibrary()

  object NativeApi extends Api with NetworkBindings with EngineBindings with NativeRequirements:

    @exported("socket_network")
    override def socketNetwork[ID](
        deviceId: ID,
        port: Int,
        neighbors: Map[ID, Endpoint],
    ): ConnectionOrientedNetworkManager[ID] = super.socketNetwork(deviceId, port, neighbors)

    @exported("engine")
    @nowarn("msg=unused local definition")
    override def engine[ID, Result](
        deviceId: ID,
        network: ConnectionOrientedNetworkManager[ID],
        program: Function1[FullLibrary, Result],
        onResult: Function1[Result, Outcome[Boolean]],
    ): Outcome[Unit] =
      try super.engine(deviceId, network, program, onResult)
      catch
        case e: Exception =>
          scribe.error(s"Error starting engine: ${e.getMessage}")
          ()
  end NativeApi
end NativeScafiRuntime
