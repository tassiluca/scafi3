#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "scafi3.h"

int device_id = 100;
int another_value = 42;

int rounds = 10;

bool on_result(const void* result) {
    printf("on_result called!\n");
    const char* message = (const char*) result;
    printf("Result: message is %s\n", message);
    free((void*) message);
    // const BinaryCodable* local_id = (const BinaryCodable*) result;
    // printf("Result: local id is %d\n", *(int*) local_id->data);
    return rounds-- > 0;
}

void* true_branch(void) { 
    char* message = malloc(5);
    strcpy(message, "True");
    printf("True branch taken %p\n", (void*) message);
    return message; 
}
void* false_branch(void) { 
    char* message = malloc(6);
    strcpy(message, "False");
    printf("False branch taken %p\n", (void*) message);
    return message; 
}

const void* aggregate_program(const AggregateLibrary* lib) {
    sleep(1); // slow down a bit...
    const SharedData* sd = lib->device();
    printf("Shared data: {default=%d}\n", *(int*) sd->default_value->data);
    return lib->branch(device_id % 2 == 0, true_branch, false_branch);
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
