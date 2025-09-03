"use strict";

import { Runtime, returnSending } from '../../../target/fastLinkJS/main.mjs';
import proto from './messages.js';

const deviceId = 3
const port = 5053
const neighbors = new Map([
    [1, Runtime.Endpoint("localhost", 5051)],
])

function prettyPrint(temp) {
    return `TemperatureSensor(id=${temp.id}, temperature=${temp.temperature})`;
}

function senseTemperature() {
    const temp = proto.TemperatureSensor.create({ id: `temp#${deviceId}`, temperature: Math.random() * 100 });
    return temp;
}

function aggregateProgram(lang) {
    const field = lang.branch(
        lang.localId % 2 == 0,
        () => lang.exchange(lang.Field.of(true), n => returnSending(n)),
        () => lang.exchange(lang.Field.of(senseTemperature()), n => returnSending(n)),
    );
    return `
        Default:   ${prettyPrint(field.default)}, 
        Neighbors: ${Array.from(field.neighborValues, ([k, v]) => `${k}: ${prettyPrint(v)}`).join(", ")}
    `;
}

const network = Runtime.socketNetwork(deviceId, port, neighbors);
Runtime.engine(deviceId, network, lang => aggregateProgram(lang), result => {
    console.log("::: Result :::");
    console.log(result);
    return true;
});
