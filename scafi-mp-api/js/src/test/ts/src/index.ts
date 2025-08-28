import { jungle, HasCodec, Codable } from "../../../../target/scala-3.6.4/scafi-mp-api-fastopt/main.mjs";
import { Foo as ProtoFoo, FooSchema } from "./foo_pb.js";
import { create, fromBinary, toBinary } from "@bufbuild/protobuf";

class Foo implements HasCodec<Foo, Uint8Array> {
    private instance: ProtoFoo;

    constructor(name: string, id: number) {
        this.instance = create(FooSchema, { name, id });
    }

    get codable(): Codable<Foo, Uint8Array> {
        return Foo.codable;
    }

    static codable: Codable<Foo, Uint8Array> = {
        typeName: "Foo",
        encode: (foo) => toBinary(FooSchema, foo.instance),
        decode: (bytes) => {
            const decodedFoo = fromBinary(FooSchema, bytes);
            return new Foo(decodedFoo.name, decodedFoo.id);
        },
    };
}

const foo = new Foo("Alycia", 42);
console.log(foo);
const backFoo = jungle(foo);
console.log(backFoo);
