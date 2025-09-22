package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, CFuncPtr1, Ptr, Zone }

import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.message.UniversalCodable
import it.unibo.scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import it.unibo.scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import it.unibo.scafi.types.{ ExportedNativeTypes, NativeTypes }
import it.unibo.scafi.utils.CUtils.withLogging

object NativeScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeRequirements extends Requirements:
    type AggregateLibrary = Ptr[ExportedNativeTypes.CAggregateLibrary]

    override given [Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
      override def encode(value: Value): Format = ???
      override def decode(data: Format): Value = ???
      override def register(value: Value): Unit = ???

    override def library[ID]: ExchangeAggregateContext[ID] ?=> AggregateLibrary = null.asInstanceOf[AggregateLibrary]

  object NativeApi extends Api with NetworkBindings with EngineBindings with NativeRequirements:

    @exported("socket_network")
    override def socketNetwork[ID](
        deviceId: ID,
        port: Int,
        neighbors: Map[ID, Endpoint],
    ): ConnectionOrientedNetworkManager[ID] = super.socketNetwork(deviceId, port, neighbors)

    @exported("engine")
    def nativeEngine[ID, Result](
        deviceId: ID,
        network: ConnectionOrientedNetworkManager[ID],
        program: CFuncPtr1[AggregateLibrary, Result],
        onResult: CFuncPtr1[Result, Outcome[Boolean]],
    ): Outcome[Unit] = withLogging(engine(deviceId, network, Zone(program(_)), onResult(_)))
  end NativeApi
end NativeScafiRuntime
