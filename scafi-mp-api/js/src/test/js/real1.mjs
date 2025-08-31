"use strict";

import { Runtime, returnSending } from '../../../target/scala-3.6.4/scafi-mp-api-fastopt/main.mjs'

console.log("::: SCAFI3 PROGRAM :::")

const deviceId = 1
const port = 5051
const neighbors = new Map([
    [2, Runtime.Endpoint("localhost", 5052)],
])

console.log("::: Info :::");
console.log("Device ID: " + deviceId);
console.log("Port:      " + port);

function aggregateProgram(lang) {
    return lang.branch(
        deviceId % 2 == 0, 
        () => lang.exchange(lang.Field.of(1), n => returnSending(n)),
        () => lang.exchange(lang.Field.of(0), n => returnSending(n)),
    );
}

const network = Runtime.socketNetwork(deviceId, port, neighbors);
Runtime.engine(deviceId, network, lang => aggregateProgram(lang));
