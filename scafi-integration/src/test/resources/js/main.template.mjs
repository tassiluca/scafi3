"use strict";
/*
 * WARNING: This is a template file intended to be used for testing purposes.
 *          It is not meant to be executed directly but rather to be processed by replacing the placeholders
 *          `{{ var }}` with actual values and injecting the necessary aggregate program to run.
 */

const { Runtime, returning, returnSending } = await import(process.env.SCAFI3);

const deviceId = {{ deviceId }};
const port = {{ port }};
const neighbors = new Map({{ neighbors }});
const network = Runtime.socketNetwork(deviceId, port, neighbors);

let lastResult = null;
let iterations = 10;
await Runtime.engine(deviceId, network, lang => aggregateProgram(lang), async result => {
    lastResult = result;
    await sleep(500);
    return iterations-- > 0;
});

console.log(lastResult.toString());

async function sleep(ms) {
    await new Promise(resolve => setTimeout(resolve, ms));
}
