"use strict";
/*
 * WARNING: This is a template file intended to be used for testing purposes.
 *          It is not meant to be executed directly but rather to be processed by replacing
 *          the placeholders `{{ var }}` with actual values.
 */

const { Runtime, returnSending } = await import(process.env.SCAFI3);

const deviceId = {{ deviceId }};
const port = {{ port }};
const neighbors = new Map({{ neighbors }});

console.log("::: Info :::");
console.log("Device ID: ", deviceId);
console.log("Port:      ", port);
console.log(
    "Neighbors: ",
    Array.from(neighbors.entries()).map(([id, end]) => `Device ${id} @ ${end.address}:${end.port}`).join(", ")
);

const network = Runtime.socketNetwork(deviceId, port, neighbors);
let lastResult = null;
let iterations = 10;
Runtime.engine(deviceId, network, lang => aggregateProgram(lang), result => {
    lastResult = result;
    console.log(`Iteration ${iterations} result: ${result}`);
    return iterations-- > 0;
});
