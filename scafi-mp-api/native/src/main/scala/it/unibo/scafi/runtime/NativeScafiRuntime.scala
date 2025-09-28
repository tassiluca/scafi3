package it.unibo.scafi.runtime

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, fromCString, CFuncPtr2, CInt, CString, CStruct2, CVoidPtr, Ptr }

import it.unibo.scafi
import it.unibo.scafi.types.EqPtr

import io.github.iltotore.iron.refineUnsafe
import libscafi3.structs.BinaryCodable

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
        case eq: EqPtr => nativeBinaryCodable.register(eq.ptr.asInstanceOf[Ptr[BinaryCodable]])
        case _ =>
          scribe.warn("Registering a Ptr[?] without EqPtr. This should not happen")
          ???
      override def encode(value: Value): Format = value match
        case eq: EqPtr => nativeBinaryCodable.encode(eq.ptr.asInstanceOf[Ptr[BinaryCodable]]).asInstanceOf[Format]
        case _ =>
          scribe.warn("Encoding a Ptr[?] without EqPtr. This should not happen")
          ???
      override def decode(bytes: Format): Value =
        val binaryCodableInstance = nativeBinaryCodable.decode(bytes.asInstanceOf[Array[Byte]])
        EqPtr(
          binaryCodableInstance.asInstanceOf[CVoidPtr],
          (!binaryCodableInstance).are_equals.asInstanceOf[CFuncPtr2[CVoidPtr, CVoidPtr, Boolean]],
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
    ): ConnectionOrientedNetworkManager[EqPtr] = socketNetwork(
      EqPtr(
        deviceId,
        (!deviceId.asInstanceOf[Ptr[BinaryCodable]]).are_equals.asInstanceOf[CFuncPtr2[CVoidPtr, CVoidPtr, Boolean]],
      ),
      port,
      neighbors,
    )

    @exported("engine")
    def nativeEngine(
        deviceId: CVoidPtr,
        network: ConnectionOrientedNetworkManager[EqPtr],
        program: Function1[AggregateLibrary, CVoidPtr],
        onResult: Function1[CVoidPtr, Outcome[Boolean]],
    ): Outcome[Unit] = engine(
      EqPtr(
        deviceId,
        (!deviceId.asInstanceOf[Ptr[BinaryCodable]]).are_equals.asInstanceOf[CFuncPtr2[CVoidPtr, CVoidPtr, Boolean]],
      ),
      network,
      program,
      onResult,
    )
  end NativeApi
end NativeScafiRuntime
