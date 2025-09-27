package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, CInt, CString, CStruct2, CVoidPtr, Ptr }

import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.libraries.FullLibrary
import it.unibo.scafi.message.UniversalCodable
import it.unibo.scafi.presentation.NativeBinaryCodable.nativeBinaryCodable
import it.unibo.scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import it.unibo.scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import it.unibo.scafi.types.NativeTypes

object NativeScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeRequirements extends Requirements:
    type AggregateLibrary = Ptr[FullLibrary#CAggregateLibrary]

    override given [Value, Format]: UniversalCodable[Value, Format] =
      nativeBinaryCodable.asInstanceOf[UniversalCodable[Value, Format]]

    override def library[ID]: ExchangeAggregateContext[ID] ?=> AggregateLibrary = FullLibrary().asNative

  object NativeApi extends Api with NetworkBindings with EngineBindings with NativeRequirements:

    type CEndpoint = CStruct2[
      /* address */ CString,
      /* port */ CInt,
    ]

    @exported("socket_network")
    def nativeSocketNetwork(
        deviceId: CVoidPtr,
        port: CInt,
        neighbors: Map[CVoidPtr, Ptr[CEndpoint]],
    ): ConnectionOrientedNetworkManager[CVoidPtr] = ???
    // val net = neighbors.map: (id, ep) =>
    //   val e = ep.asInstanceOf[Ptr[CEndpoint]]
    //   id -> Endpoint(fromCString(e._1), e._2)
    // socketNetwork(deviceId, port, net)

    @exported("engine")
    def nativeEngine(
        deviceId: CVoidPtr,
        network: ConnectionOrientedNetworkManager[CVoidPtr],
        program: Function1[AggregateLibrary, CVoidPtr],
        onResult: Function1[CVoidPtr, Outcome[Boolean]],
    ): Outcome[Unit] = engine(deviceId, network, program, onResult)
  end NativeApi
end NativeScafiRuntime
