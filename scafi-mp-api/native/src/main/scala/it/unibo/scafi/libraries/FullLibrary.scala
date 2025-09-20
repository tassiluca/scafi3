package it.unibo.scafi.libraries

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.types.NativeTypes

class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData,
) extends FullPortableLibrary
    with PortableFieldBasedSharedData
    with NativeTypes:

  override given valueCodable[Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
    override def encode(value: Value): Format = ???
    override def decode(data: Format): Value = ???
    override def register(value: Value): Unit = ???
