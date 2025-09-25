#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "scafi3.h"

int device_id = 100;
int rounds = 10;

bool on_result(const void* result) {
    const BinaryCodable* local_id = (const BinaryCodable*) result;
    printf("Result: local id is %d\n", *(int*) local_id->data);
    return rounds-- > 0;
}

const void* aggregate_program(const AggregateLibrary* lib) {
    sleep(1); // slow down a bit...
    return lib->local_id();
}

int main(void) {
    BinaryCodable bin_device_id = {
        .data = &device_id,
        .serialize = NULL,
        .deserialize = NULL
    };
    printf("Calling with device id %p\n", &bin_device_id);
    printf("My device id is %d\n", * (int*) bin_device_id.data);
    const ConnectionOrientedNetworkManager* network = socket_network(&bin_device_id, 9000, Connections_empty());
    engine(&bin_device_id, network, aggregate_program, on_result);
    printf("OK!\n");
    return 0;
}
