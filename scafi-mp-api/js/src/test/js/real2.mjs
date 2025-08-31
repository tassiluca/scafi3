"use strict";

import { Runtime, returnSending } from '../../../target/scala-3.6.4/scafi-mp-api-fastopt/main.mjs'

console.log("::: SCAFI3 PROGRAM :::")

const deviceId = 2
const port = 5052
const neighbors = new Map([
    [1, Runtime.Endpoint("localhost", 5051)],
])

console.log("::: Info :::");
console.log("Device ID: " + deviceId);
console.log("Port:      " + port);

function aggregateProgram(lang) {
    return lang.exchange(lang.Field.of(lang.localId), n => returnSending(n));
}

const network = Runtime.socketNetwork(deviceId, port, neighbors);
Runtime.engine(deviceId, network, lang => aggregateProgram(lang));
