package it.unibo.scafi.presentation

import java.nio.charset.StandardCharsets

import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

import it.unibo.scafi.presentation.protobufjs.ProtobufJSType.fromProtobufJsMessage
import it.unibo.scafi.utils.JSUtils.{ asDynamic, toByteArray, toUint8Array }

import io.bullet.borer.{ Cbor, Codec, Decoder, Encoder }
import io.bullet.borer.derivation.ArrayBasedCodecs.deriveCodec

/**
 * A base trait for defining format- and library-agnostic binary codables for JavaScript objects.
 *
 * Any object passed to the library intended to be encoded or decoded in any format of choice is expected to have a
 * static members conforming this trait structure, i.e., providing a `typeName` property and coherent `encode` and
 * `decode` methods. Alternatively, the object can delegate the (de)serialization concerns to a property named
 * `codable`.
 *
 * On JavaScript, if using `protobuf.js`, it is possible to use the generated message types directly.
 *
 * <h1>Examples</h1>
 *
 * In the following examples, we show how it is possible to define valid codables for both JavaScript and Typescript.
 *
 * <h2>Plain JavaScript</h2>
 *
 * For JSON-based encoding/decoding:
 *
 * {{{
 * class Person {
 *     constructor(name, surname) {
 *         this.name = name;
 *         this.surname = surname;
 *     }
 *
 *     static typeName = "Person"
 *
 *     static encode(person) {
 *         return JSON.stringify({
 *             name: person.name,
 *             surname: person.surname
 *         });
 *     }
 *
 *     static decode(bytes) {
 *         return JSON.parse(bytes);
 *     }
 * }
 * }}}
 *
 * <h2>Typescript</h2>
 *
 * Using protobuf messages:
 * {{{
 * class Foo implements HasCodec<Foo, Uint8Array> {
 *     private instance: ProtoFoo;
 *
 *     constructor(name: string, id: number) {
 *         this.instance = create(FooSchema, { name, id });
 *     }
 *
 *     get codable(): Codable<Foo, Uint8Array> {
 *         return Foo.codable;
 *     }
 *
 *     static codable: Codable<Foo, Uint8Array> = {
 *         typeName: "Foo",
 *         encode: (foo) => toBinary(FooSchema, foo.instance),
 *         decode: (bytes) => {
 *             const decodedFoo = fromBinary(FooSchema, bytes);
 *             return new Foo(decodedFoo.name, decodedFoo.id);
 *         },
 *     };
 * }
 * }}}
 */
trait JSCodable extends js.Object:

  /** The name of the type this codable can encode/decode. */
  def typeName: String

  /**
   * Encodes the provided message into a well-known format.
   * @param message
   *   the message to encode
   * @return
   *   the encoded message
   */
  def encode(message: js.Object): js.Any

  /**
   * Decodes the provided data into a JavaScript object.
   * @param data
   *   the encoded data to decode
   * @return
   *   the decoded JavaScript object
   */
  def decode(data: js.Any): js.Object
end JSCodable

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object JSCodable:

  /**
   * Creates a `JSCodable` instance from the provided JavaScript object, if possible.
   * @param message
   *   the JavaScript object to convert into a `JSCodable`
   * @return
   *   a new `JSCodable` instance
   * @throws IllegalArgumentException
   *   if the provided object does not conform to the `JSCodable` structure.
   */
  def apply(message: js.Object): JSCodable = message.asJSCodable.getOrElse:
    throw new IllegalArgumentException("The provided value is not a valid `JSBinaryCodable`.")

  extension (message: js.Object)
    def asJSCodable: Option[JSCodable] = message.fromProtobufJsMessage.orElse(message.asPlainJSCodable)

    def asPlainJSCodable: Option[JSCodable] = message.asValidatedJSCodable.map(_.asInstanceOf[JSCodable])

    private def asValidatedJSCodable: Option[js.Dynamic] =
      def valid(Type: js.Dynamic): Option[js.Dynamic] =
        if !js.isUndefined(message) &&
          js.typeOf(message) == "object" &&
          js.typeOf(Type.encode) == "function" &&
          js.typeOf(Type.decode) == "function" &&
          js.typeOf(Type.typeName) == "string"
        then Some(Type)
        else None
      valid(message.asDynamic.constructor).orElse(valid(message.asDynamic.codable))

  given jsBinaryCodable: RegisterableCodable[js.Object, Array[Byte]] = new RegisterableCodable[js.Object, Array[Byte]]:
    private val registry = JSCodablesRegistry()

    override def register(value: js.Object): Unit = registry.register(JSCodable(value))

    enum Format derives CanEqual:
      case Binary, String

    given Codec[Format] = deriveCodec[Format]

    override def encode(value: js.Object): Array[Byte] =
      val codable = JSCodable(value)
      val (format, encodedData) = codable.encode(value) match
        case bytes: Uint8Array => (Format.Binary, bytes.toByteArray)
        case s if js.typeOf(s) == "string" => (Format.String, s.asInstanceOf[String].getBytes(StandardCharsets.UTF_8))
        case other => throw new IllegalArgumentException(s"$other (${js.typeOf(other)}) is not a supported format.")
      Cbor.encode(codable.typeName, format, encodedData).toByteArray

    override def decode(bytes: Array[Byte]): scala.scalajs.js.Object =
      val (typeName, format, encodedData) = Cbor.decode(bytes).to[(String, Format, Array[Byte])].value
      registry.get(typeName) match
        case Some(codable) =>
          format match
            case Format.Binary => codable.decode(encodedData.toUint8Array)
            case Format.String => codable.decode(new String(encodedData, StandardCharsets.UTF_8))
        case None => throw new IllegalStateException(s"Unknow type: $typeName. This should not happen. Report this.")
end JSCodable
