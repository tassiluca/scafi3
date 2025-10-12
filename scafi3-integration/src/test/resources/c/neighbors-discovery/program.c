const void* aggregate_program(const AggregateLibrary* lang) {
    return lang->exchange(lang->Field.of(lang->local_id()), lambda(ReturnSending, (const Field* f), {
        return return_sending(f, lang->Field.of(lang->local_id()));
    }));
}
