package it.unibo.scafi.runtime

import scala.concurrent.ExecutionContext
import scala.scalajs.js

import it.unibo.scafi

import io.github.iltotore.iron.refineUnsafe

import scafi.context.xc.ExchangeAggregateContext
import scafi.message.UniversalCodable
import scafi.presentation.JSBinaryCodable.jsBinaryCodable
import scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import scafi.libraries.{ FullLibrary, JSTypes }

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object JSScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with JSTypes:

  given ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

  trait JSAdts extends Adts:
    override type DeviceId = js.Any

    @JSExport @JSExportAll
    case class Endpoint(address: String, port: Int)

    override given deviceIdIso[ID]: Iso[DeviceId, ID] =
      Iso((id: DeviceId) => id.asInstanceOf[ID])((id: ID) => id.asInstanceOf[DeviceId])

    override given toInetEndpoint: Conversion[Endpoint, scafi.runtime.network.sockets.InetTypes.Endpoint] = e =>
      scafi.runtime.network.sockets.InetTypes.Endpoint(e.address.refineUnsafe, e.port.refineUnsafe)

  trait JSRequirements extends Requirements with NoMemorySafeContext with JSAdts:
    override type AggregateLibrary = FullLibrary

    override given deviceIdCodable[Format]: UniversalCodable[DeviceId, Format] =
      jsBinaryCodable.asInstanceOf[UniversalCodable[DeviceId, Format]]

    override def library[ID](using Arena): ExchangeAggregateContext[ID] ?=> FullLibrary = FullLibrary()

  @JSExportTopLevel("Runtime")
  object JSAPI extends Api with NetworkBindings with EngineBindings with JSRequirements
end JSScafiRuntime
