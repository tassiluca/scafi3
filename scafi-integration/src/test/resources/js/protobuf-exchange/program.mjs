import proto from "./messages.js";

function aggregateProgram(lang) {
    const temperature = senseTemperature();
    return lang.exchange(lang.Field.of(temperature), n =>
        returning(n).send(lang.Field.of(temperature))
    );
}

proto.TemperatureSensor.prototype.toString = function () {
    return `TemperatureSensor(id=${this.id}, temperature=${this.temperature})`;
};

function senseTemperature() {
    return proto.TemperatureSensor.create({ id: `temp#${deviceId}`, temperature: deviceId * 10 });
}
