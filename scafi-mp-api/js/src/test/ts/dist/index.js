"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const main_mjs_1 = require("../../../../target/scala-3.6.4/scafi-mp-api-fastopt/main.mjs");
const foo_pb_js_1 = require("./foo_pb.js");
const protobuf_1 = require("@bufbuild/protobuf");
class Foo {
    _foo;
    constructor(name, id) {
        this._foo = (0, protobuf_1.create)(foo_pb_js_1.FooSchema, { name, id });
    }
    codable = {
        encode: (f) => (0, protobuf_1.toBinary)(foo_pb_js_1.FooSchema, f._foo),
        decode: (b) => {
            const decodedFoo = (0, protobuf_1.fromBinary)(foo_pb_js_1.FooSchema, b);
            return new Foo(decodedFoo.name, decodedFoo.id);
        },
        typeName: "Foo",
    };
}
const foo = new Foo("Alycia", 42);
console.log(foo);
const backFoo = (0, main_mjs_1.jungle)(foo);
console.log(backFoo);
//# sourceMappingURL=index.js.map