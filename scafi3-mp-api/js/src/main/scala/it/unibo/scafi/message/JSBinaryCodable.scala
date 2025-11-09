package it.unibo.scafi.message

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

import it.unibo.scafi.utils.Uint8ArrayOps.{ toByteArray, toUint8Array }

@SuppressWarnings(Array("DisableSyntax.asInstanceOf"))
object JSBinaryCodable:

  /**
   * A universal codable that can encode and decode in binary format any JavaScript object that either corresponds to a
   * primitive type (i.e., `string`, `number`, or `boolean`), or implements the [[JSCodable]] interface.
   */
  given jsCodable: Conversion[js.Any, Codable[js.Any, Any]] = value =>
    new Codable[js.Any, Any]:
      private val codable = JSCodable(value)

      override def encode(value: js.Any): Any =
        codable.encode(value) match
          case bytes: Uint8Array => bytes.toByteArray
          case other => throw new IllegalArgumentException(s"$other (${js.typeOf(other)}) is not a supported format.")

      override def decode(data: Any): js.Any =
        data match
          case bytes: Array[Byte] => codable.decode(bytes.toUint8Array)
          case _ => throw new IllegalArgumentException(s"$data (${js.typeOf(data)}) is not a supported format.")
end JSBinaryCodable
