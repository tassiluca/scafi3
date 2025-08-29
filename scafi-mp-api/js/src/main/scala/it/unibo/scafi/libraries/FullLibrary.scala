package it.unibo.scafi.libraries

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.presentation.JSCodable.jsBinaryCodable
import it.unibo.scafi.presentation.RegisterableCodable

/**
 * A JavaScript version of the full Scafi library.
 * @param lang
 *   the language context providing the necessary syntax and semantics for all the libraries.
 */
@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
@JSExportTopLevel("FullLibrary")
class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData,
) extends FullPortableLibrary
    with PortableFieldBasedSharedData
    with JSTypes:

  override given valueCodable[Value, Format]: CodableWithRegister[Value, Format] =
    jsBinaryCodable.asInstanceOf[RegisterableCodable[Value, Format]]
