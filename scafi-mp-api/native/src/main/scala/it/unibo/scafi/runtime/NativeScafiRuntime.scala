package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, fromCString, CInt, CString, CStruct2, CVoidPtr, Ptr }

import it.unibo.scafi
import scafi.context.xc.ExchangeAggregateContext
import scafi.libraries.FullLibrary
import scafi.message.UniversalCodable
import scafi.presentation.NativeBinaryCodable.nativeBinaryCodable
import scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import scafi.types.NativeTypes

import io.github.iltotore.iron.refineUnsafe

object NativeScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeRequirements extends Requirements:
    type AggregateLibrary = Ptr[FullLibrary#CAggregateLibrary]

    override given [Value, Format]: UniversalCodable[Value, Format] =
      nativeBinaryCodable.asInstanceOf[UniversalCodable[Value, Format]]

    override def library[ID]: ExchangeAggregateContext[ID] ?=> AggregateLibrary = FullLibrary().asNative

  trait NativeAdts extends Adts:
    override type Endpoint = Ptr[
      CStruct2[
        /* address */ CString,
        /* port */ CInt,
      ],
    ]

    override given asInetEndpoint: Conversion[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] =
      e => scafi.runtime.network.sockets.InetTypes.Endpoint(fromCString(e._1).refineUnsafe, e._2.refineUnsafe)

  object NativeApi extends Api with NativeAdts with NetworkBindings with EngineBindings with NativeRequirements:

    @exported("socket_network")
    def nativeSocketNetwork(
        deviceId: CVoidPtr,
        port: CInt,
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
