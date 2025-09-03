/*eslint-disable block-scoped-var, id-length, no-control-regex, no-magic-numbers, no-prototype-builtins, no-redeclare, no-shadow, no-var, sort-vars*/
"use strict";

var $protobuf = require("protobufjs/minimal");

// Common aliases
var $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;

// Exported root namespace
var $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

$root.TemperatureSensor = (function() {

    /**
     * Properties of a TemperatureSensor.
     * @exports ITemperatureSensor
     * @interface ITemperatureSensor
     * @property {string|null} [id] TemperatureSensor id
     * @property {number|null} [temperature] TemperatureSensor temperature
     */

    /**
     * Constructs a new TemperatureSensor.
     * @exports TemperatureSensor
     * @classdesc Represents a TemperatureSensor.
     * @implements ITemperatureSensor
     * @constructor
     * @param {ITemperatureSensor=} [properties] Properties to set
     */
    function TemperatureSensor(properties) {
        if (properties)
            for (var keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                if (properties[keys[i]] != null)
                    this[keys[i]] = properties[keys[i]];
    }

    /**
     * TemperatureSensor id.
     * @member {string} id
     * @memberof TemperatureSensor
     * @instance
     */
    TemperatureSensor.prototype.id = "";

    /**
     * TemperatureSensor temperature.
     * @member {number} temperature
     * @memberof TemperatureSensor
     * @instance
     */
    TemperatureSensor.prototype.temperature = 0;

    /**
     * Creates a new TemperatureSensor instance using the specified properties.
     * @function create
     * @memberof TemperatureSensor
     * @static
     * @param {ITemperatureSensor=} [properties] Properties to set
     * @returns {TemperatureSensor} TemperatureSensor instance
     */
    TemperatureSensor.create = function create(properties) {
        return new TemperatureSensor(properties);
    };

    /**
     * Encodes the specified TemperatureSensor message. Does not implicitly {@link TemperatureSensor.verify|verify} messages.
     * @function encode
     * @memberof TemperatureSensor
     * @static
     * @param {ITemperatureSensor} message TemperatureSensor message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    TemperatureSensor.encode = function encode(message, writer) {
        if (!writer)
            writer = $Writer.create();
        if (message.id != null && Object.hasOwnProperty.call(message, "id"))
            writer.uint32(/* id 1, wireType 2 =*/10).string(message.id);
        if (message.temperature != null && Object.hasOwnProperty.call(message, "temperature"))
            writer.uint32(/* id 2, wireType 5 =*/21).float(message.temperature);
        return writer;
    };

    /**
     * Encodes the specified TemperatureSensor message, length delimited. Does not implicitly {@link TemperatureSensor.verify|verify} messages.
     * @function encodeDelimited
     * @memberof TemperatureSensor
     * @static
     * @param {ITemperatureSensor} message TemperatureSensor message or plain object to encode
     * @param {$protobuf.Writer} [writer] Writer to encode to
     * @returns {$protobuf.Writer} Writer
     */
    TemperatureSensor.encodeDelimited = function encodeDelimited(message, writer) {
        return this.encode(message, writer).ldelim();
    };

    /**
     * Decodes a TemperatureSensor message from the specified reader or buffer.
     * @function decode
     * @memberof TemperatureSensor
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @param {number} [length] Message length if known beforehand
     * @returns {TemperatureSensor} TemperatureSensor
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    TemperatureSensor.decode = function decode(reader, length, error) {
        if (!(reader instanceof $Reader))
            reader = $Reader.create(reader);
        var end = length === undefined ? reader.len : reader.pos + length, message = new $root.TemperatureSensor();
        while (reader.pos < end) {
            var tag = reader.uint32();
            if (tag === error)
                break;
            switch (tag >>> 3) {
            case 1: {
                    message.id = reader.string();
                    break;
                }
            case 2: {
                    message.temperature = reader.float();
                    break;
                }
            default:
                reader.skipType(tag & 7);
                break;
            }
        }
        return message;
    };

    /**
     * Decodes a TemperatureSensor message from the specified reader or buffer, length delimited.
     * @function decodeDelimited
     * @memberof TemperatureSensor
     * @static
     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
     * @returns {TemperatureSensor} TemperatureSensor
     * @throws {Error} If the payload is not a reader or valid buffer
     * @throws {$protobuf.util.ProtocolError} If required fields are missing
     */
    TemperatureSensor.decodeDelimited = function decodeDelimited(reader) {
        if (!(reader instanceof $Reader))
            reader = new $Reader(reader);
        return this.decode(reader, reader.uint32());
    };

    /**
     * Verifies a TemperatureSensor message.
     * @function verify
     * @memberof TemperatureSensor
     * @static
     * @param {Object.<string,*>} message Plain object to verify
     * @returns {string|null} `null` if valid, otherwise the reason why it is not
     */
    TemperatureSensor.verify = function verify(message) {
        if (typeof message !== "object" || message === null)
            return "object expected";
        if (message.id != null && message.hasOwnProperty("id"))
            if (!$util.isString(message.id))
                return "id: string expected";
        if (message.temperature != null && message.hasOwnProperty("temperature"))
            if (typeof message.temperature !== "number")
                return "temperature: number expected";
        return null;
    };

    /**
     * Creates a TemperatureSensor message from a plain object. Also converts values to their respective internal types.
     * @function fromObject
     * @memberof TemperatureSensor
     * @static
     * @param {Object.<string,*>} object Plain object
     * @returns {TemperatureSensor} TemperatureSensor
     */
    TemperatureSensor.fromObject = function fromObject(object) {
        if (object instanceof $root.TemperatureSensor)
            return object;
        var message = new $root.TemperatureSensor();
        if (object.id != null)
            message.id = String(object.id);
        if (object.temperature != null)
            message.temperature = Number(object.temperature);
        return message;
    };

    /**
     * Creates a plain object from a TemperatureSensor message. Also converts values to other types if specified.
     * @function toObject
     * @memberof TemperatureSensor
     * @static
     * @param {TemperatureSensor} message TemperatureSensor
     * @param {$protobuf.IConversionOptions} [options] Conversion options
     * @returns {Object.<string,*>} Plain object
     */
    TemperatureSensor.toObject = function toObject(message, options) {
        if (!options)
            options = {};
        var object = {};
        if (options.defaults) {
            object.id = "";
            object.temperature = 0;
        }
        if (message.id != null && message.hasOwnProperty("id"))
            object.id = message.id;
        if (message.temperature != null && message.hasOwnProperty("temperature"))
            object.temperature = options.json && !isFinite(message.temperature) ? String(message.temperature) : message.temperature;
        return object;
    };

    /**
     * Converts this TemperatureSensor to JSON.
     * @function toJSON
     * @memberof TemperatureSensor
     * @instance
     * @returns {Object.<string,*>} JSON object
     */
    TemperatureSensor.prototype.toJSON = function toJSON() {
        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
    };

    /**
     * Gets the default type url for TemperatureSensor
     * @function getTypeUrl
     * @memberof TemperatureSensor
     * @static
     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
     * @returns {string} The default type url
     */
    TemperatureSensor.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
        if (typeUrlPrefix === undefined) {
            typeUrlPrefix = "type.googleapis.com";
        }
        return typeUrlPrefix + "/TemperatureSensor";
    };

    return TemperatureSensor;
})();

module.exports = $root;
