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
