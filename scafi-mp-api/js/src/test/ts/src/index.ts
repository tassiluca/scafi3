import { Foo } from "./foo.js";

const foo = Foo.create({ name: "Alice", id: 42 });

console.log(Foo.toBinary(foo));

