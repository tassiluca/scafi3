package it.unibo.scafi.presentation

import scala.scalanative.unsafe.{ alloc, fromCString, CSize, Ptr, Zone }
import scala.scalanative.unsafe.Size.intToSize

import it.unibo.scafi.message.UniversalCodable
import it.unibo.scafi.utils.CUtils.{ toByteArray, toUint8Array }

import io.bullet.borer.{ Cbor, Codec }
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodec
import libscafi3.aliases.size_t
import libscafi3.structs.BinaryCodable

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object NativeBinaryCodable:

  private enum Format derives CanEqual:
    case Binary, String

  private given Codec[Format] = deriveCodec[Format]

  given nativeBinaryCodable: UniversalCodable[Ptr[BinaryCodable], Array[Byte]] =
    new UniversalCodable[Ptr[BinaryCodable], Array[Byte]]:
      private var registry = NativeCodablesRegistry.forStringId()

      override def register(value: Ptr[BinaryCodable]): Unit = registry = registry.register(value)

      override def encode(value: Ptr[BinaryCodable]): Array[Byte] = Zone:
        println(">>> Encode!")
        val typeName = fromCString((!value).type_name)
        val encodedSize = alloc[size_t]()
        val rawData = (!value).encode((!value).data, encodedSize)
        val data = rawData.toByteArray(!encodedSize)
        Cbor.encode(typeName, Format.Binary, data).toByteArray

      override def decode(bytes: Array[Byte]): Ptr[BinaryCodable] = Zone:
        val (typeName, format, encodedData) = Cbor.decode(bytes).to[(String, Format, Array[Byte])].value
        registry.get(typeName) match
          case Some(codable) =>
            format match
              case Format.Binary =>
                (!codable).decode(encodedData.toUint8Array, encodedData.length.toCSize).asInstanceOf[Ptr[BinaryCodable]]
              case Format.String => throw new IllegalStateException("String format not yet supported in native.")
          case None => throw new IllegalStateException(s"Unknow type: $typeName. This should not happen. Report this.")

end NativeBinaryCodable
