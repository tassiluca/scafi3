"use strict";

import { Runtime, returnSending } from '../../../target/scala-3.6.4/scafi-mp-api-fastopt/main.mjs'
import proto from './foo.js';

console.log("::: SCAFI3 PROGRAM :::")

const deviceId = 3
const port = 5053
const neighbors = new Map([
    [1, Runtime.Endpoint("localhost", 5051)],
])

console.log("::: Info :::");
console.log("Device ID: " + deviceId);
console.log("Port:      " + port);

const foo = proto.Foo.create({ name: "Bob", id: 22 });

function prettyPrint(foo) {
    return `Foo(name=${foo.name}, id=${foo.id})`;
}

function aggregateProgram(lang) {
    const all = lang.branch(
        deviceId % 2 == 0, 
        () => lang.exchange(lang.Field.of(true), n => returnSending(n)),
        () => lang.exchange(lang.Field.of(foo), n => returnSending(n)),
    );
    return `Default: ${prettyPrint(all.default)}, Neighbors: ${
        Array.from(all.neighborValues, ([k, v]) => `${k}: ${prettyPrint(v)}`).join(", ")
    }`;
}

const network = Runtime.socketNetwork(deviceId, port, neighbors);
Runtime.engine(deviceId, network, lang => aggregateProgram(lang));
