package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, fromCString, CInt, CString, CStruct2, CVoidPtr, Ptr }

import it.unibo.scafi
import it.unibo.scafi.types.{ CBinaryCodable, EqPtr }
import it.unibo.scafi.types.CBinaryCodable.{ equalsFn, hashFn }

import io.github.iltotore.iron.refineUnsafe

import scafi.context.xc.ExchangeAggregateContext
import scafi.libraries.FullLibrary
import scafi.message.UniversalCodable
import scafi.presentation.NativeBinaryCodable.nativeBinaryCodable
import scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import scafi.types.NativeTypes

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object NativeScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeAdts extends Adts:
    override type DeviceId = EqPtr
    override type Endpoint = Ptr[CStruct2[ /* address */ CString, /* port */ CInt]]

    override given deviceIdIso[ID]: Iso[DeviceId, ID] =
      Iso[EqPtr, ID](_.ptr.asInstanceOf[ID]):
        case p: Ptr[?] =>
          val codable = p.asInstanceOf[Ptr[CBinaryCodable]]
          EqPtr(codable, codable.equalsFn, codable.hashFn)
        case e: EqPtr => e

    override given toInetEndpoint: Conversion[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] =
      e => scafi.runtime.network.sockets.InetTypes.Endpoint(fromCString(e._1).refineUnsafe, e._2.refineUnsafe)

  trait NativeRequirements extends Requirements with NativeMemoryContext with NativeAdts:

    override type AggregateLibrary = Ptr[FullLibrary#CAggregateLibrary]

    override given deviceIdCodable[Format]: UniversalCodable[DeviceId, Format] =
      new UniversalCodable[DeviceId, Format]:
        override def register(value: DeviceId): Unit =
          nativeBinaryCodable.register(value.ptr.asInstanceOf[Ptr[CBinaryCodable]])
        override def encode(value: DeviceId): Format =
          nativeBinaryCodable.encode(value.ptr.asInstanceOf[Ptr[CBinaryCodable]]).asInstanceOf[Format]
        override def decode(bytes: Format): DeviceId =
          val binaryCodableInstance = nativeBinaryCodable.decode(bytes.asInstanceOf[Array[Byte]])
          EqPtr(
            binaryCodableInstance.asInstanceOf[CVoidPtr],
            binaryCodableInstance.equalsFn,
            binaryCodableInstance.hashFn,
          )

    override def library[ID](using Arena): ExchangeAggregateContext[ID] ?=> AggregateLibrary = FullLibrary().asNative
  end NativeRequirements

  object NativeApi extends Api with NativeAdts with NetworkBindings with EngineBindings with NativeRequirements:

    @exported("socket_network")
    def nativeSocketNetwork(
        deviceId: CVoidPtr,
        port: CInt,
        neighbors: Map[CVoidPtr, Endpoint],
    ): ConnectionOrientedNetworkManager[DeviceId] = socketNetwork[CVoidPtr](deviceId, port, neighbors)

    @exported("engine")
    def nativeEngine(
        deviceId: CVoidPtr,
        network: ConnectionOrientedNetworkManager[DeviceId],
        program: Function1[AggregateLibrary, CVoidPtr],
        onResult: Function1[CVoidPtr, Outcome[Boolean]],
    ): Outcome[Unit] = engine(deviceId, network, program, onResult)
  end NativeApi
end NativeScafiRuntime
