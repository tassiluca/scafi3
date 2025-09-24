package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, CVoidPtr, Ptr }

import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.libraries.FullLibrary
import it.unibo.scafi.message.UniversalCodable
import it.unibo.scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import it.unibo.scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import it.unibo.scafi.types.{ ExportedNativeTypes, NativeTypes }

object NativeScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeRequirements extends Requirements:
    type AggregateLibrary = Ptr[ExportedNativeTypes.CAggregateLibrary]

    override given [Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
      override def encode(value: Value): Format = ???
      override def decode(data: Format): Value = ???
      override def register(value: Value): Unit = ???

    override def library[ID]: ExchangeAggregateContext[ID] ?=> AggregateLibrary = FullLibrary().asNative

  object NativeApi extends Api with NetworkBindings with EngineBindings with NativeRequirements:

    @exported("socket_network")
    def nativeSocketNetwork(
        deviceId: CVoidPtr,
        port: Int,
        neighbors: Map[CVoidPtr, Endpoint],
    ): ConnectionOrientedNetworkManager[CVoidPtr] = socketNetwork(deviceId, port, neighbors)

    @exported("engine")
    def nativeEngine(
        deviceId: CVoidPtr,
        network: ConnectionOrientedNetworkManager[CVoidPtr],
        program: Function1[AggregateLibrary, CVoidPtr],
        onResult: Function1[CVoidPtr, Outcome[Boolean]],
    ): Outcome[Unit] = engine(deviceId, network, program, onResult)
  end NativeApi
end NativeScafiRuntime
