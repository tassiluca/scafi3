package it.unibo.scafi.presentation

import java.nio.charset.StandardCharsets

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

import it.unibo.scafi.message.UniversalCodable
import it.unibo.scafi.presentation.primitive.PrimitiveCodables.{ asPrimitiveCodable, primitiveCodables }
import it.unibo.scafi.utils.JSUtils.{ toByteArray, toUint8Array }

import io.bullet.borer.{ Cbor, Codec }
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodec

@SuppressWarnings(Array("DisableSyntax.asInstanceOf"))
object JSBinaryCodable:

  given jsBinaryCodable: UniversalCodable[js.Any, Array[Byte]] = new UniversalCodable[js.Any, Array[Byte]]:
    private var registry = JSCodablesRegistry.forStringId(primitiveCodables)

    override def register(value: js.Any): Unit =
      val codable = value.asPrimitiveCodable.getOrElse(JSCodable(value.asInstanceOf[js.Object]))
      registry = registry.register(codable)

    enum Format derives CanEqual:
      case Binary, String

    given Codec[Format] = deriveCodec[Format]

    override def encode(value: js.Any): Array[Byte] =
      val codable = value.asPrimitiveCodable.getOrElse(JSCodable(value.asInstanceOf[js.Object]))
      val (format, encodedData) = codable.encode(value.asInstanceOf[js.Object]) match
        case bytes: Uint8Array => (Format.Binary, bytes.toByteArray)
        case s if js.typeOf(s) == "string" => (Format.String, s.asInstanceOf[String].getBytes(StandardCharsets.UTF_8))
        case other => throw new IllegalArgumentException(s"$other (${js.typeOf(other)}) is not a supported format.")
      Cbor.encode(codable.typeName, format, encodedData).toByteArray

    override def decode(bytes: Array[Byte]): js.Any =
      val (typeName, format, encodedData) = Cbor.decode(bytes).to[(String, Format, Array[Byte])].value
      registry.get(typeName) match
        case Some(codable) =>
          format match
            case Format.Binary => codable.decode(encodedData.toUint8Array)
            case Format.String => codable.decode(new String(encodedData, StandardCharsets.UTF_8))
        case None => throw new IllegalStateException(s"Unknow type: $typeName. This should not happen. Report this.")
end JSBinaryCodable
