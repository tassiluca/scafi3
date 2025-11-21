package it.unibo.scafi.runtime

import java.nio.charset.StandardCharsets

import scala.concurrent.ExecutionContext

import it.unibo.scafi
import it.unibo.scafi.message.BinaryCodable
import it.unibo.scafi.types.JSTypes

import io.github.iltotore.iron.refineUnsafe

import scafi.context.xc.ExchangeAggregateContext
import scafi.runtime.bindings.ScafiEngineBinding
import scafi.libraries.FullLibrary

object JSScafiRuntime extends PortableRuntime with ScafiEngineBinding with JSTypes:

  given ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

  trait JSAdts extends Adts:
    override type DeviceId = Int

    override given BinaryCodable[DeviceId] = new BinaryCodable[DeviceId]:
      def encode(msg: DeviceId): Array[Byte] = msg.toString.getBytes(StandardCharsets.UTF_8)
      def decode(bytes: Array[Byte]): DeviceId = new String(bytes, StandardCharsets.UTF_8).toInt

    @JSExport @JSExportAll
    case class Endpoint(address: String, port: Int)

    override given toInetEndpoint: Conversion[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] = e =>
      scafi.runtime.network.sockets.InetTypes.Endpoint(e.address.refineUnsafe, e.port.refineUnsafe)

  trait JSRequirements extends Requirements with NoMemorySafeContext with JSAdts:
    override type AggregateLibrary = FullLibrary

    override def library(using Arena): ExchangeAggregateContext[DeviceId] ?=> FullLibrary = FullLibrary()

  @JSExportTopLevel("Runtime")
  object JSAPI extends Api with EngineBindings with JSRequirements
end JSScafiRuntime
