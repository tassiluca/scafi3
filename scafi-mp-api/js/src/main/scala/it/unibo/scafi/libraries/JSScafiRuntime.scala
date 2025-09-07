package it.unibo.scafi.libraries

import scala.concurrent.ExecutionContext

import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.message.RegisterableCodable
import it.unibo.scafi.presentation.JSBinaryCodable.jsBinaryCodable
import it.unibo.scafi.runtime.PortableRuntime
import it.unibo.scafi.runtime.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object JSScafiRuntime extends PortableRuntime with ScafiNetworkBinding with ScafiEngineBinding with JSTypes:

  override given ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

  trait JSRequirements extends Requirements:
    type AggregateLibrary = FullLibrary

    override given [Value, Format]: RegisterableCodable[Value, Format] =
      jsBinaryCodable.asInstanceOf[RegisterableCodable[Value, Format]]

    override def library[ID]: ExchangeAggregateContext[ID] ?=> FullLibrary = FullLibrary()

  @JSExportTopLevel("Runtime")
  object JSAPI extends Api with Adts with NetworkBindings with EngineBindings with JSRequirements
