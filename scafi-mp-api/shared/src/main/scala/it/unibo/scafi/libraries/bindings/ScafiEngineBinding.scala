package it.unibo.scafi.libraries.bindings

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import it.unibo.scafi.context.xc.ExchangeAggregateContext.exchangeContextFactory
import it.unibo.scafi.libraries.{ PortableRuntime, PortableTypes }
import it.unibo.scafi.runtime.ScafiEngine
import it.unibo.scafi.runtime.network.sockets.ConnectionOrientedNetworkManager

trait ScafiEngineBinding[AggregateLibrary] extends PortableRuntime[AggregateLibrary]:
  self: PortableTypes =>

  given ExecutionContext = compiletime.deferred

  trait EngineBindings extends Interface:
    self: ADTs =>

    override def engine[ID, Result](
        deviceId: ID,
        network: ConnectionOrientedNetworkManager[ID],
        program: Function1[AggregateLibrary, Result],
        onResult: Function1[Result, Handler[Boolean]],
    ): Handler[Unit] =
      def round: Future[Unit] =
        for
          engine = ScafiEngine(deviceId, network, exchangeContextFactory)(program(library))
          result <- Future(engine.cycle())
          continue <- onResult(result)
          _ <- if continue then round else Future.unit
        yield ()
      network
        .start()
        .flatMap(_ => round)
        .andThen(_ => network.close())
        .andThen:
          case Success(_) => Console.out.println("Engine stopped successfully.")
          case Failure(err) => Console.err.println(s"Error occurred: ${err.getMessage}")
    end engine
  end EngineBindings
end ScafiEngineBinding
