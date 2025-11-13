const void* aggregate_program(const AggregateLibrary* lang) {
    return lang->neighbor_values(int_of({{ deviceId }}));
}
