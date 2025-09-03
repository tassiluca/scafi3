"use strict";

import { Runtime, returnSending } from '../../../target/fastLinkJS/main.mjs';

const deviceId = 3
const port = 5053
const neighbors = new Map([
    [1, Runtime.Endpoint("localhost", 5051)],
])

function aggregateProgram(lang) {
    return lang.exchange(lang.Field.of(lang.localId), n =>
        returnSending(n)
    );
}

const network = Runtime.socketNetwork(deviceId, port, neighbors);
Runtime.engine(deviceId, network, lang => aggregateProgram(lang), result => {
    console.log("::: Result :::");
    console.log("  Default: ", result.default);
    console.log("  Neighbors: ", Array.from(result.neighborValues).map(([k, v]) => `${k} -> ${v}`).join(", "));
    return true;
});
