import { jungle, registerProtobufType } from '../../../target/scala-3.6.4/scafi-mp-api-fastopt/main.mjs'
import proto from './foo.js';
import protobuf from "protobufjs";

console.log("Hello world from shitty manual test");

async function createDynamicRegistry() {
    const root = await protobuf.load("./foo.proto");
    root.nestedArray.forEach(nested => {
        if (nested instanceof protobuf.Type) {
            console.log("Loaded type:", nested.name);
            registerProtobufType(nested.name, nested);
        }
    });
}

await createDynamicRegistry()

const foo = proto.Foo.create({ name: "Alice", id: 42 });
console.log(foo);

console.log("Total bytes: " + proto.Foo.encode(foo).finish().length);

console.log("Now the funny part...")
const fooback = jungle(foo);
console.log(fooback);

console.log(fooback.name);
console.log(fooback.id);
