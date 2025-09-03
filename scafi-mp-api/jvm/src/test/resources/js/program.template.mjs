"use strict";

const { Runtime, returnSending } = await import(process.env.SCAFI3);

const deviceId = {{ deviceId }};
const port = {{ port }};
const neighbors = new Map({{ neighbors }});

console.log("::: Info :::");
console.log("Device ID: " + deviceId);
console.log("Port:      " + port);
console.log("Neighbors: " + JSON.stringify(neighbors, null, 2));

const network = Runtime.socketNetwork(deviceId, port, neighbors);
let iterations = 10;
Runtime.engine(deviceId, network, lang => aggregateProgram(lang), result => {
    console.log("::: Result :::");
    console.log("  Default: ", result.default);
    console.log("  Neighbors: ", Array.from(result.neighborValues).map(([k, v]) => `${k} -> ${v}`).join(", "));
    return iterations-- > 0;
});
