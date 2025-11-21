package it.unibo.scafi.runtime

import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.scalanative.unsafe.{ exported, fromCString, CInt, CVoidPtr, Ptr }

import it.unibo.scafi
import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.libraries.FullLibrary
import it.unibo.scafi.message.BinaryCodable
import it.unibo.scafi.nativebindings.structs.{ AggregateLibrary as CAggregateLibrary, Endpoint as CEndpoint }
import it.unibo.scafi.runtime.bindings.ScafiEngineBinding
import it.unibo.scafi.runtime.network.sockets.InetTypes
import it.unibo.scafi.types.NativeTypes

import io.github.iltotore.iron.refineUnsafe

object NativeScafiRuntime extends PortableRuntime with ScafiEngineBinding with NativeTypes:

  given ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  trait NativeAdts extends Adts:
    override type DeviceId = CInt

    override given BinaryCodable[DeviceId] = new BinaryCodable[DeviceId]:
      def encode(msg: DeviceId): Array[Byte] = msg.toString.getBytes(StandardCharsets.UTF_8)
      def decode(bytes: Array[Byte]): DeviceId = new String(bytes, StandardCharsets.UTF_8).toInt

    override type Endpoint = Ptr[CEndpoint]

    override given toInetEndpoint: Conversion[Endpoint, InetTypes.Endpoint] = e =>
      InetTypes.Endpoint(fromCString((!e).address).refineUnsafe, (!e).port.refineUnsafe)

  trait NativeRequirements extends Requirements with NativeMemoryContext with NativeAdts:

    override type AggregateLibrary = Ptr[CAggregateLibrary]

    override def library(using Arena): ExchangeAggregateContext[DeviceId] ?=> AggregateLibrary =
      FullLibrary().asNative

  object NativeApi extends Api with NativeAdts with EngineBindings with NativeRequirements:

    @exported("engine")
    def nativeEngine(
        deviceId: DeviceId,
        port: Int,
        neighbors: Map[DeviceId, Endpoint],
        program: Function1[AggregateLibrary, CVoidPtr],
        onResult: Function1[CVoidPtr, Outcome[Boolean]],
    ): Outcome[Unit] = engine(deviceId, port, neighbors, program, onResult)
end NativeScafiRuntime
