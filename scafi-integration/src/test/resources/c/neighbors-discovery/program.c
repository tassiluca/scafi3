#include "scafi3.h"

const void* aggregate_program(const AggregateLibrary* lang) {
    return lang->exchange(lang->Field.of(lang->local_id()), lambda(ReturnSending, (const SharedData* in), {
        return return_sending(in, lang->Field.of(lang->local_id()));
    }));
}
