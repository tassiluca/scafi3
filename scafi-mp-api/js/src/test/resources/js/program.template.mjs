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
Runtime.engine(deviceId, network, lang => aggregateProgram(lang));
