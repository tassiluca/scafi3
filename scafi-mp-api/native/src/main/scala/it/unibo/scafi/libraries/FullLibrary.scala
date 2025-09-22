package it.unibo.scafi.libraries

import scala.scalanative.unsafe.Ptr

import it.unibo.scafi.language.AggregateFoundation
import it.unibo.scafi.language.common.syntax.BranchingSyntax
import it.unibo.scafi.language.xc.FieldBasedSharedData
import it.unibo.scafi.language.xc.syntax.ExchangeSyntax
import it.unibo.scafi.types.{ ExportedNativeTypes, NativeTypes, NativeTypesConversions }

class FullLibrary(using
    lang: AggregateFoundation & ExchangeSyntax & BranchingSyntax & FieldBasedSharedData,
) extends FullPortableLibrary
    with PortableFieldBasedSharedData
    with NativeTypes
    with NativeTypesConversions:

  override given valueCodable[Value, Format]: UniversalCodable[Value, Format] = new UniversalCodable[Value, Format]:
    override def encode(value: Value): Format =
      scribe.info(s"[Full library] Encoding value: $value")
      ???
    override def decode(data: Format): Value =
      scribe.info(s"[Full library] Decoding data: $data")
      ???
    override def register(value: Value): Unit =
      scribe.info(s"[Full library] Registering value: $value")
      ???

  def asNative: Ptr[ExportedNativeTypes.CAggregateLibrary] = nativeAggregateLibrary()
end FullLibrary
