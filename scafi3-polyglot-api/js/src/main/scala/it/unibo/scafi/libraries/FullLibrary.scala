package it.unibo.scafi.libraries

import scala.scalajs.js.annotation.JSExportTopLevel

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.language.fc.syntax.FieldCalculusSyntax
import it.unibo.scafi.language.xc.{ FieldBasedSharedData, JSFieldBasedSharedData }
import it.unibo.scafi.message.JSCodable
import it.unibo.scafi.message.JSCodable.jsAnyCodable
import it.unibo.scafi.types.JSTypes

/**
 * A JavaScript version of the full Scafi library.
 * @param lang
 *   the language context providing the necessary syntax and semantics for all the libraries.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
@JSExportTopLevel("FullLibrary")
class FullLibrary(using
    lang: AggregateFoundation & BranchingSyntax & FieldBasedSharedData & FieldCalculusSyntax & { type DeviceId = Int },
) extends FullPortableLibrary
    with JSFieldBasedSharedData
    with JSTypes:

  override type Codec[Value, Format] = JSCodable

  override given codecOf[Format, Value <: Codec[Value, Format]]: Conversion[Value, Codable[Value, Format]] =
    v => jsAnyCodable(v).asInstanceOf[Codable[Value, Format]]
