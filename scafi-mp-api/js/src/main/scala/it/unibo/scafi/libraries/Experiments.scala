package it.unibo.scafi.libraries

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

import it.unibo.scafi.message.Codable
import it.unibo.scafi.message.CodableFromTo
import it.unibo.scafi.message.Codable.*
import it.unibo.scafi.presentation.JSCodable.jsBinaryCodable
import it.unibo.scafi.presentation.RegisterableCodable

trait Experiments:

  given valueCodable: [Value, Format] => RegisterableCodable[Value, Format] = compiletime.deferred

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object CrazyExperiments extends Experiments:

  override given valueCodable[Value, Format]: RegisterableCodable[Value, Format] =
    jsBinaryCodable.asInstanceOf[RegisterableCodable[Value, Format]]

  @JSExportTopLevel("jungle")
  def jungle[Value](value: Value): Value =
    println("::: Jungle 🏝️ :::")
    valueCodable.register(value)
    businessLogic(value)

  private def businessLogic[Format, Value: CodableFromTo[Format]](value: Value): Value =
    println("::: Business Logic 💼 :::")
    val encoded = encode(value)
    val decoded = decode(encoded)
    decoded
end CrazyExperiments
