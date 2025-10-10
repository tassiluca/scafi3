const void* aggregate_program(const AggregateLibrary* lang) {
    const void* true_field(void) {
        const Field* true_const_field = lang->Field.of(codable(1));
        return lang->exchange(true_const_field, lambda(ReturnSending, (const Field* f), {
            return return_sending(f, true_const_field);
        }));
    }

    const void* false_field(void) {
        const Field* true_const_field = lang->Field.of(codable(0));
        return lang->exchange(true_const_field, lambda(ReturnSending, (const Field* f), {
            return return_sending(f, true_const_field);
        }));
    }

    return lang->branch({{ deviceId }} % 2 == 0, true_field, false_field);
}
