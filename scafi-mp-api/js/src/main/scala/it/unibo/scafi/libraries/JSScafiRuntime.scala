package it.unibo.scafi.libraries

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.concurrent.duration.DurationInt
import scala.concurrent.Future

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
    ): Future[Unit] =
      def loop(): Future[Unit] =
        for
          engine = ScafiEngine(deviceId, network, exchangeContextFactory)(program(FullLibrary()))
          result <- Future(engine.cycle())
          _ = scribe.info(s"Node $deviceId cycle result: $result")
          _ <- Platform.asyncOps.sleep(1.second)
          _ <- loop()
        yield ()
      network
        .start()
        .andThen(res => scribe.info("Network started " + res))
        .flatMap(_ => loop())
        .andThen(res => scribe.info(res.toString))
    end engine
  end JSInterface
end JSScafiRuntime
