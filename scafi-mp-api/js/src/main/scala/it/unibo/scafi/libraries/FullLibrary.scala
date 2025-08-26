package it.unibo.scafi.libraries

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.message.Codable
import it.unibo.scafi.presentation.protobuf.ProtobufCodable.given

object ShittyTest:

  @JSExportTopLevel("jungle")
  def jungle[Value](value: Value): Value =
    println("::: Jungle 🏝️ :::")
    val encoded = protoCodable.encode(value.asInstanceOf[js.Object])
    println("Encoded bytes: " + encoded.mkString(" "))
    println("Total bytes: " + encoded.length)
    val decoded = protoCodable.decode(encoded)
    decoded.asInstanceOf[Value]

/**
 * A JavaScript version of the full Scafi library.
 * @param lang
 *   the language context providing the necessary syntax and semantics for all the libraries.
 */
@JSExportTopLevel("FullLibrary")
class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData,
) extends FullPortableLibrary
    with PortableFieldBasedSharedData
    with JSTypes:

  override given [Value, Format]: Codable[Value, Format] = new Codable[Value, Format]:
    // TODO: completely unsafe! To support more Formats?
    override def encode(value: Value): Format =
      protoCodable.encode(value.asInstanceOf[js.Object]).asInstanceOf[Format]
    override def decode(data: Format): Value =
      protoCodable.decode(data.asInstanceOf[Array[Byte]]).asInstanceOf[Value]
