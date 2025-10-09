package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, fromCString, CInt, CString, CStruct2, CVoidPtr, Ptr }

import it.unibo.scafi
import it.unibo.scafi.message.CBinaryCodable
import it.unibo.scafi.message.CBinaryCodable.given_Hash_Ptr
import it.unibo.scafi.types.EqWrapper

import io.github.iltotore.iron.refineUnsafe

import scafi.context.xc.ExchangeAggregateContext
import scafi.libraries.FullLibrary
import scafi.message.UniversalCodable
import scafi.message.NativeBinaryCodable.nativeBinaryCodable
import scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import scafi.types.NativeTypes

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object NativeScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeAdts extends Adts:
    override type DeviceId = EqWrapper[Ptr[CBinaryCodable]]

    override given [ID] => Conversion[ID, DeviceId] = id => EqWrapper(id.asInstanceOf[Ptr[CBinaryCodable]])

    override type Endpoint = Ptr[CStruct2[ /* address */ CString, /* port */ CInt]]

    override given toInetEndpoint: Conversion[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] =
      e => scafi.runtime.network.sockets.InetTypes.Endpoint(fromCString(e._1).refineUnsafe, e._2.refineUnsafe)

  trait NativeRequirements extends Requirements with NativeMemoryContext with NativeAdts:

    override type AggregateLibrary = Ptr[FullLibrary#CAggregateLibrary]

    override given deviceIdCodable[Format]: UniversalCodable[DeviceId, Format] =
      new UniversalCodable[DeviceId, Format]:
        override def register(id: DeviceId): Unit = nativeBinaryCodable.register(id.value)
        override def encode(id: DeviceId): Format = nativeBinaryCodable.encode(id.value).asInstanceOf[Format]
        override def decode(data: Format): DeviceId =
          EqWrapper(nativeBinaryCodable.decode(data.asInstanceOf[Array[Byte]]))

    override def library[ID](using Arena): ExchangeAggregateContext[ID] ?=> AggregateLibrary = FullLibrary().asNative

  object NativeApi extends Api with NativeAdts with NetworkBindings with EngineBindings with NativeRequirements:

    @exported("socket_network")
    def nativeSocketNetwork(
        deviceId: CVoidPtr,
        port: CInt,
        neighbors: Map[CVoidPtr, Endpoint],
    ): ConnectionOrientedNetworkManager[DeviceId] = socketNetwork[CVoidPtr](deviceId, port, neighbors)

    @exported("engine")
    def nativeEngine(
        network: ConnectionOrientedNetworkManager[DeviceId],
        program: Function1[AggregateLibrary, CVoidPtr],
        onResult: Function1[CVoidPtr, Outcome[Boolean]],
    ): Outcome[Unit] = engine(network, program, onResult)
end NativeScafiRuntime
