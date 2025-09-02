"use strict";

import { Runtime } from '#scafi3';

const deviceId = {{deviceId}};
const port = {{port}};
const neighbors = new Map({{neighbors}});

console.log("::: Info :::");
console.log("Device ID: " + deviceId);
console.log("Port:      " + port);
console.log("Neighbors: " + neighbors);

const network = Runtime.socketNetwork(deviceId, port, neighbors);
Runtime.engine(deviceId, network, lang => aggregateProgram(lang));
