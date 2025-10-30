/*
 * Warning: this file is a template used for testing purposes and it is not intended to be executed directly.
 * It will be processed by the test suite to replace placeholders (i.e., `{{ ... }}`) with actual values and
 * inject the aggregate program to be tested.
 */
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#ifdef _WIN32
    #include <windows.h>
    #define sleep(x) Sleep((x) * 1000)
#else
    #include <unistd.h>
#endif
#include "scafi3.h"
#include "utils.h"
#include "int_codable.c"

#define ITERATIONS 10

const void* aggregate_program(const AggregateLibrary* lang);

bool on_result(const void* result) {
    static int round = 0;
    sleep(1);
    if (round == ITERATIONS) {
        printf("%s", field_to_str((const Field*) result));
        return false;
    } else {
        round += 1;
        return true;
    }
}

int main(void) {
    Neighborhood neighbors = Neighborhood_empty();
    {{ neighbors }}
    engine(device({{ deviceId }}), {{ port }}, neighbors, aggregate_program, on_result);
    // printf("%s", field_to_str((const Field*) last_round_result));
    return 0;
}
