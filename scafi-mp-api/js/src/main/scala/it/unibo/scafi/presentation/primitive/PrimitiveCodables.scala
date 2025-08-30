package it.unibo.scafi.presentation.primitive

import java.nio.charset.StandardCharsets

import it.unibo.scafi.presentation.JSCodable
import it.unibo.scafi.utils.JSUtils.{ toByteArray, toUint8Array }

import scalajs.js
import scalajs.js.typedarray.Uint8Array

@SuppressWarnings(Array("DisableSyntax.asInstanceOf"))
object PrimitiveCodables:

  extension (message: js.Any)
    def asPrimitiveCodable: Option[JSCodable] = js.typeOf(message) match
      case value if value == "number" =>
        val codable = new JSCodable:
          override def typeName: String = value
          override def encode(message: js.Object): js.Any =
            message.toString.getBytes(StandardCharsets.UTF_8).toUint8Array.asInstanceOf[js.Any]
          override def decode(data: js.Any): js.Object =
            val res = new String(data.asInstanceOf[Uint8Array].toByteArray, StandardCharsets.UTF_8).toDouble
            res.asInstanceOf[js.Object]
        Some(codable)
      case value if value == "boolean" =>
        val codable = new JSCodable:
          override def typeName: String = value
          override def encode(message: js.Object): js.Any =
            val binaryValue = if message.asInstanceOf[Boolean] then 1 else 0
            binaryValue.toString.getBytes(StandardCharsets.UTF_8).toUint8Array.asInstanceOf[js.Any]
          override def decode(data: js.Any): js.Object =
            val res = new String(data.asInstanceOf[Uint8Array].toByteArray, StandardCharsets.UTF_8).toInt match
              case 1 => true
              case _ => false
            res.asInstanceOf[js.Object]
        Some(codable)
      case str if str == "string" =>
        val codable = new JSCodable:
          override def typeName: String = str
          override def encode(message: js.Object): js.Any =
            message.toString.getBytes(StandardCharsets.UTF_8).asInstanceOf[js.Any]
          override def decode(data: js.Any): js.Object =
            new String(data.asInstanceOf[Array[Byte]], StandardCharsets.UTF_8).asInstanceOf[js.Object]
        Some(codable)
      case _ => None
  end extension
end PrimitiveCodables
