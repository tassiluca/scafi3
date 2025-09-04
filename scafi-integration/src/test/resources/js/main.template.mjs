"use strict";
/*
 * WARNING: This is a template file intended to be used for testing purposes.
 * 
 * Every occurrence of {{ var }} will be replaced with the value of the variable `var` in the test infrastructure.
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
let iterations = 10;
Runtime.engine(deviceId, network, lang => aggregateProgram(lang), result => {
    console.log("::: Result :::");
    console.log("  Default: ", result.default);
    console.log("  Neighbors: ", Array.from(result.neighborValues).map(([k, v]) => `${k} -> ${v}`).join(", "));
    return iterations-- > 0;
});
