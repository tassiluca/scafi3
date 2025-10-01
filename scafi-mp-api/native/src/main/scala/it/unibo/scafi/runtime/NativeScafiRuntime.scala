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

  trait NativeRequirements extends Requirements:
    type AggregateLibrary = Ptr[FullLibrary#CAggregateLibrary]

    override given [Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
      override def register(value: Value): Unit = value match
        case eq: EqPtr => nativeBinaryCodable.register(eq.ptr.asInstanceOf[Ptr[CBinaryCodable]])
        case _ =>
          scribe.warn("Registering a Ptr[?] without EqPtr. This should not happen")
          ???
      override def encode(value: Value): Format = value match
        case eq: EqPtr => nativeBinaryCodable.encode(eq.ptr.asInstanceOf[Ptr[CBinaryCodable]]).asInstanceOf[Format]
        case _ =>
          scribe.warn("Encoding a Ptr[?] without EqPtr. This should not happen")
          ???
      override def decode(bytes: Format): Value =
        val binaryCodableInstance = nativeBinaryCodable.decode(bytes.asInstanceOf[Array[Byte]])
        EqPtr(
          binaryCodableInstance.asInstanceOf[CVoidPtr],
          binaryCodableInstance.equalsFn,
          binaryCodableInstance.hashFn,
        ).asInstanceOf[Value]

    override def library[ID]: ExchangeAggregateContext[ID] ?=> AggregateLibrary = FullLibrary().asNative
  end NativeRequirements

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
    ): ConnectionOrientedNetworkManager[EqPtr] =
      val deviceIdAsBinaryCodable = deviceId.asInstanceOf[Ptr[CBinaryCodable]]
      nativeBinaryCodable.register(deviceIdAsBinaryCodable)
      socketNetwork(EqPtr(deviceId, deviceIdAsBinaryCodable.equalsFn, deviceIdAsBinaryCodable.hashFn), port, neighbors)

    @exported("engine")
    def nativeEngine(
        deviceId: CVoidPtr,
        network: ConnectionOrientedNetworkManager[EqPtr],
        program: Function1[AggregateLibrary, CVoidPtr],
        onResult: Function1[CVoidPtr, Outcome[Boolean]],
    ): Outcome[Unit] =
      val deviceIdAsBinaryCodable = deviceId.asInstanceOf[Ptr[CBinaryCodable]]
      engine(
        EqPtr(deviceId, deviceIdAsBinaryCodable.equalsFn, deviceIdAsBinaryCodable.hashFn),
        network,
        program,
        onResult,
      )
  end NativeApi
end NativeScafiRuntime
