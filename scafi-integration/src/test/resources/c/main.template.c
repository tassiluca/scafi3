/*
 * Warning: this file is a template used for testing purposes and it is not intended to be executed directly.
 * It will be processed by the test suite to replace placeholders (i.e., `{{ ... }}`) with actual values and
 * inject the aggregate program to be tested.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#ifdef _WIN32
    #include <windows.h>
    #define sleep(x) Sleep((x) * 1000)
#else
    #include <unistd.h>
#endif
#include "scafi3.h"

#define ITERATIONS 10
#define PORT {{ port }}

int iterations = ITERATIONS;

int device_id = {{ deviceId }};

const void* last_result = NULL;

static bool int_compare(const void* a, const void* b) {
    const BinaryCodable* v1 = (const BinaryCodable*)a;
    const BinaryCodable* v2 = (const BinaryCodable*)b;
    return *(int*)v1->data == *(int*)v2->data;
}

static uint8_t* encode_int(void* data, size_t* encoded_size) {
    if (!data || !encoded_size) return NULL;
    *encoded_size = sizeof(int);
    uint8_t *buffer = malloc(*encoded_size);
    if (!buffer) return NULL;
    memcpy(buffer, data, sizeof(int));
    return buffer;
}

static char* int_str(const void* data) {
    if (!data) return NULL;
    BinaryCodable* bc = (BinaryCodable*) data;
    int value = *(int*) bc->data;
    size_t needed = snprintf(NULL, 0, "%d", value) + 1;
    char* str = malloc(needed);
    if (!str) return NULL;
    snprintf(str, needed, "%d", value);
    return str;
}

static BinaryCodable* decode_int(const uint8_t* buffer, size_t size) {
    if (!buffer || size != sizeof(int)) return NULL;
    int decoded_value;
    memcpy(&decoded_value, buffer, sizeof(int));
    BinaryCodable *bc = malloc(sizeof(BinaryCodable));
    if (!bc) return NULL;
    int *stored_value = malloc(sizeof(int));
    if (!stored_value) {
        free(bc);
        return NULL;
    }
    *stored_value = decoded_value;
    bc->data = stored_value;
    bc->type_name = "int";
    bc->encode = encode_int;
    bc->decode = decode_int;
    bc->equals = int_compare;
    bc->to_str = int_str;
    return bc;
}

BinaryCodable int_codable_of(int value) {
    int* stored_value = malloc(sizeof(int));
    if (!stored_value) return (BinaryCodable){0};
    *stored_value = value;
    return (BinaryCodable) {
        .data = stored_value,
        .type_name = "int",
        .encode = encode_int,
        .decode = decode_int,
        .equals = int_compare,
        .to_str = int_str
    };
}

BinaryCodable DEVICE_ID;

// ------------------------------- TEST PROGRAM -------------------------------

const void* aggregate_program(const AggregateLibrary* lang);

bool on_result(const void* result) {
    last_result = result;
    sleep(1);
    return iterations-- > 0;
}

int main(void) {
    DEVICE_ID = int_codable_of(device_id);
    Neighborhood neighbors = Neighborhood_empty();
    {{ neighbors }}
    ConnectionOrientedNetworkManager network = socket_network(&DEVICE_ID, PORT, neighbors);
    engine(&DEVICE_ID, network, aggregate_program, on_result);
    const SharedData* final_result = (const SharedData*) last_result;
    printf("%s", shared_data_to_string(final_result));
    return 0;
}
