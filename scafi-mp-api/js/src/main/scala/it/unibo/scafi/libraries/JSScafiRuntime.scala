package it.unibo.scafi.libraries

import scala.concurrent.ExecutionContext

import it.unibo.scafi.context.xc.ExchangeAggregateContext
import it.unibo.scafi.libraries.bindings.{ ScafiEngineBinding, ScafiNetworkBinding }
import it.unibo.scafi.message.RegisterableCodable
import it.unibo.scafi.presentation.JSBinaryCodable.jsBinaryCodable

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object JSScafiRuntime
    extends PortableRuntime[FullLibrary]
    with ScafiNetworkBinding[FullLibrary]
    with ScafiEngineBinding[FullLibrary]
    with JSTypes:

  override given ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

  override given [Value, Format]: RegisterableCodable[Value, Format] =
    jsBinaryCodable.asInstanceOf[RegisterableCodable[Value, Format]]

  override def library[ID](using ctx: ExchangeAggregateContext[ID]): FullLibrary = FullLibrary()

  @JSExportTopLevel("Runtime")
  object JSInterface extends Interface with ADTs with NetworkBindings with EngineBindings
