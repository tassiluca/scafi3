package it.unibo.scafi.presentation.primitive

import java.nio.charset.StandardCharsets

import it.unibo.scafi.presentation.JSCodable
import it.unibo.scafi.utils.JSUtils.{ toByteArray, toUint8Array }

import scalajs.js
import scalajs.js.typedarray.Uint8Array

@SuppressWarnings(Array("DisableSyntax.asInstanceOf"))
object PrimitiveCodables:

  private given stringCodable: JSCodable = new JSCodable:
    override def typeName: String = "string"
    override def encode(message: js.Object): js.Any =
      message.toString.getBytes(StandardCharsets.UTF_8).asInstanceOf[js.Any]
    override def decode(data: js.Any): js.Object =
      new String(data.asInstanceOf[Array[Byte]], StandardCharsets.UTF_8).asInstanceOf[js.Object]

  private given numberCodable: JSCodable = new JSCodable:
    override def typeName: String = "number"
    override def encode(message: js.Object): js.Any =
      message.toString.getBytes(StandardCharsets.UTF_8).toUint8Array.asInstanceOf[js.Any]
    override def decode(data: js.Any): js.Object =
      new String(data.asInstanceOf[Uint8Array].toByteArray, StandardCharsets.UTF_8).toDouble.asInstanceOf[js.Object]

  private given booleanCodable: JSCodable = new JSCodable:
    override def typeName: String = "boolean"
    override def encode(message: js.Object): js.Any =
      val value = if message.asInstanceOf[Boolean] then 1 else 0
      value.toString.getBytes(StandardCharsets.UTF_8).toUint8Array.asInstanceOf[js.Any]
    override def decode(data: js.Any): js.Object =
      val value = new String(data.asInstanceOf[Uint8Array].toByteArray, StandardCharsets.UTF_8).toInt == 1
      value.asInstanceOf[js.Object]

  extension (message: js.Any)
    def asPrimitiveCodable: Option[JSCodable] = js.typeOf(message) match
      case t if t == "number" => Some(numberCodable)
      case t if t == "boolean" => Some(booleanCodable)
      case t if t == "string" => Some(stringCodable)
      case _ => None
end PrimitiveCodables
