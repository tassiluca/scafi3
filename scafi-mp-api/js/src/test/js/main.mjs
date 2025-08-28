"use strict";

import { jungle } from '../../../target/scala-3.6.4/scafi-mp-api-fastopt/main.mjs'
import proto from './foo.js';

console.log(typeof JSON.stringify({ name: "Alice", id: 42 })); // string

// it works both with custom message complying with JSBinaryCodable

class Person {
    constructor(name, surname) {
        this.name = name;
        this.surname = surname;
    }
    
    static typeName = "Person"

    static encode(person) {
        return JSON.stringify({
            name: person.name,
            surname: person.surname
        });
    }

    static decode(bytes) {
        return JSON.parse(bytes);
    }
}

const p = new Person("John", "Doe");
console.log(p);
const pback = jungle(p);
console.log(pback);

console.log("----");
// or with protobuf.js that have a compatible interface

const foo = proto.Foo.create({ name: "Alice", id: 42 });
console.log(foo);

const fooback = jungle(foo);
console.log(fooback);
console.log(fooback.name);
console.log(fooback.id);
