const void* aggregate_program(const AggregateLibrary* lang) {
    const Field* initial = lang->Field.of(device({{ deviceId }}));
    return lang->exchange(initial, fn(ReturnSending*, (const Field* nvalues), {
        const Field* to_send = lang->Field.of(device({{ deviceId }}));
        return return_sending(nvalues, to_send);
    }));
}
