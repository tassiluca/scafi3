package it.unibo.scafi.libraries

import scala.concurrent.{ ExecutionContext, Future }
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }

import it.unibo.scafi.libraries.bindings.ScafiNetworkBinding
import it.unibo.scafi.message.RegisterableCodable
import it.unibo.scafi.presentation.JSBinaryCodable.jsBinaryCodable
import it.unibo.scafi.runtime.ScafiEngine
import it.unibo.scafi.context.xc.ExchangeAggregateContext.exchangeContextFactory
import it.unibo.scafi.runtime.network.sockets.ConnectionOrientedNetworkManager
import it.unibo.scafi.utils.Platform

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object JSScafiRuntime extends PortableRuntime with ScafiNetworkBinding with JSTypes:

  override given ExecutionContext = scalajs.concurrent.JSExecutionContext.Implicits.queue

  override given [Value, Format]: RegisterableCodable[Value, Format] =
    jsBinaryCodable.asInstanceOf[RegisterableCodable[Value, Format]]

  @JSExportTopLevel("Runtime")
  object JSInterface extends Interface with ADTs with NetworkBindings:

    @JSExport
    def engine[ID, Result](
        deviceId: ID,
        network: ConnectionOrientedNetworkManager[ID],
        program: Function1[FullLibrary, Result],
        onResult: Function1[Result, Boolean],
    ): Unit =
      def loop(): Future[Unit] =
        for
          engine = ScafiEngine(deviceId, network, exchangeContextFactory)(program(FullLibrary()))
          result <- Future(engine.cycle())
          _ <- Platform.asyncOps.sleep(500.millis)
          continue = onResult(result)
          _ <- if continue then loop() else Future.unit
        yield ()
      network
        .start()
        .flatMap(_ => loop())
        .andThen(_ => network.close())
        .onComplete:
          case Success(_) => scribe.info("Engine stopped successfully.")
          case Failure(err) => scribe.error(s"Error occurred: ${err.getMessage}")
    end engine
  end JSInterface
end JSScafiRuntime
