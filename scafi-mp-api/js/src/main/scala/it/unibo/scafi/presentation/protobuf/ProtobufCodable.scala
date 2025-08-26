package it.unibo.scafi.presentation.protobuf

import it.unibo.scafi.message.Codable
import it.unibo.scafi.utils.{ toByteArray, toUint8Array }

import scalajs.js
import io.bullet.borer.Cbor
import scala.scalajs.js.annotation.JSExportTopLevel

object ProtobufCodable:
  private var typesRegistry = Map.empty[String, ProtobufType[js.Object]]

  def useProtobufEncoding(protoFilePath: String): Unit = ???

  @JSExportTopLevel("registerProtobufType")
  def register(name: String, message: js.Object): Unit =
    val Type = message.asInstanceOf[ProtobufType[js.Object]]
    typesRegistry += (name -> Type)

  given protoCodable: Codable[js.Object, Array[Byte]] with

    def encode(value: scala.scalajs.js.Object): Array[Byte] =
      val Type = ProtobufType(value)
      val messageTypeName = Type.getTypeUrl("").split("/").last
      val encodedValue = Type.encode(value).finish().toByteArray
      Cbor.encode((messageTypeName, encodedValue)).toByteArray

    def decode(data: Array[Byte]): js.Object =
      val (messageTypeName, encodedValue) = Cbor.decode(data).to[(String, Array[Byte])].value
      val Type = typesRegistry.getOrElse(
        messageTypeName,
        throw new IllegalStateException(s"""
          Unknown Protobuf message type: $messageTypeName.
          Are you sure you have registered the type?
        """),
      )
      Type.decode(toUint8Array(encodedValue))
end ProtobufCodable
