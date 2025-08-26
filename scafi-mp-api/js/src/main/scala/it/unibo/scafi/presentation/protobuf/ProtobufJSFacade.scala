package it.unibo.scafi.presentation.protobuf

import scalajs.js
import js.typedarray.Uint8Array

/**
 * A Protobuf message type.
 */
@js.native
trait ProtobufType[Message <: js.Object] extends js.Object:

  /**
   * Verifies the specified message.
   * @param message
   *   plain object to verify
   * @return
   *   `null` if valid, otherwise the reason why it is not
   */
  def verify(message: Message): Null | String = js.native

  /**
   * Encodes the specified message. Does not implicitly verify messages.
   * @param Message
   *   plain object to encode
   * @return
   *   the writer to encode to
   */
  def encode(message: Message): Writer = js.native

  /**
   * Decodes the specified binary data.
   * @param bytes
   *   the buffer to decode from
   * @return
   *   the decoded message
   * @throws Error
   *   if the payload is not a valid buffer or if the required fields are missing
   */
  def decode(bytes: Uint8Array): Message = js.native

  /**
   * Gets the default type url for Foo
   * @param typeUrlPrefix
   *   your custom prefix (default "type.googleapis.com")
   * @return
   *   the default type url for the message in the <typeUrlPrefix>/<messageType>
   */
  def getTypeUrl(typeUrlPrefix: String): String = js.native
end ProtobufType

object ProtobufType:

  def apply(message: js.Object): ProtobufType[js.Object] =
    val messageType = message.asInstanceOf[js.Dynamic].constructor
    if !js.isUndefined(message) &&
      js.typeOf(message) == "object" &&
      js.typeOf(messageType.encode) == "function" &&
      js.typeOf(messageType.decode) == "function" &&
      js.typeOf(messageType.verify) == "function"
    then messageType.asInstanceOf[ProtobufType[js.Object]]
    else throw new IllegalArgumentException("Expected a Protobuf message, got " + js.typeOf(message))

/**
 * Wire format writer.
 */
@js.native
trait Writer extends js.Object:

  /**
   * Finishes the write operation.
   * @return
   *   the finished buffer
   */
  def finish(): Uint8Array = js.native
