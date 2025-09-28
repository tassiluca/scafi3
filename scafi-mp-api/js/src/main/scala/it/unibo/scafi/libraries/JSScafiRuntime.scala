package it.unibo.scafi.libraries

import scala.concurrent.ExecutionContext

import it.unibo.scafi

import io.github.iltotore.iron.refineUnsafe

import scafi.context.xc.ExchangeAggregateContext
import scafi.message.UniversalCodable
import scafi.presentation.JSBinaryCodable.jsBinaryCodable
import scafi.runtime.PortableRuntime
import scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object JSScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with JSTypes:

  given ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

  trait JSRequirements extends Requirements:
    type AggregateLibrary = FullLibrary

    override given [Value, Format]: UniversalCodable[Value, Format] =
      jsBinaryCodable.asInstanceOf[UniversalCodable[Value, Format]]

    override def library[ID]: ExchangeAggregateContext[ID] ?=> FullLibrary = FullLibrary()

  trait JSAdts extends Adts:
    @JSExport @JSExportAll
    case class Endpoint(address: String, port: Int)

    override given asInetEndpoint: Conversion[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] = e =>
      scafi.runtime.network.sockets.InetTypes.Endpoint(e.address.refineUnsafe, e.port.refineUnsafe)

  @JSExportTopLevel("Runtime")
  object JSAPI extends Api with JSAdts with NetworkBindings with EngineBindings with JSRequirements
end JSScafiRuntime
