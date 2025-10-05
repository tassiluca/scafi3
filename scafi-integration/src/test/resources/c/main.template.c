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
#include "utils.h"

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
static bool codable_int_compare(const void* a, const void* b) {
    const BinaryCodable *bc_a = (const BinaryCodable*)a;
    const BinaryCodable *bc_b = (const BinaryCodable*)b;
    return *(int*) bc_a->data == *(int*) bc_b->data;
}

static uint32_t codable_int_hash(const void* a) {
    const BinaryCodable *bc_a = (const BinaryCodable*)a;
    return (uint32_t)(*(int*) bc_a->data);
}

static uint8_t* encode_int(void* data, size_t* encoded_size) {
    if (!data || !encoded_size) return NULL;
    *encoded_size = sizeof(int);
    uint8_t *buffer = malloc(sizeof(int));
    return buffer ? memcpy(buffer, data, sizeof(int)) : NULL;
}

static char* codable_int_str(const void* data) {
    if (!data) return NULL;
    int value = *(int*)((BinaryCodable*)data)->data;
    size_t str_len = snprintf(NULL, 0, "%d", value) + 1; // +1 for null terminator
    char* str = malloc(str_len);
    return str && snprintf(str, str_len, "%d", value) > 0 ? str : (free(str), NULL);
}

static BinaryCodable* decode_int(const uint8_t* buffer, size_t size) {
    if (!buffer || size != sizeof(int)) return NULL;
    BinaryCodable *bc = malloc(sizeof(BinaryCodable) + sizeof(int));
    if (!bc) return NULL;
    bc->data = (int*)(bc + 1); // Store int right after struct
    memcpy(bc->data, buffer, sizeof(int));
    bc->type_name = "int";
    bc->encode = encode_int;
    bc->decode = decode_int;
    bc->equals = codable_int_compare;
    bc->hash = codable_int_hash;
    bc->to_str = codable_int_str;
    return bc;
}

BinaryCodable* codable_int(int value) {
    BinaryCodable *bc = malloc(sizeof(BinaryCodable) + sizeof(int));
    if (!bc) return NULL;
    bc->data = (int*)(bc + 1);  // Data right after struct
    *(int*)bc->data = value;
    bc->type_name = "int";
    bc->encode = encode_int;
    bc->decode = decode_int;
    bc->equals = codable_int_compare;
    bc->hash = codable_int_hash;
    bc->to_str = codable_int_str;
    return bc;
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#define ITERATIONS 10

BinaryCodable* DEVICE_ID;

const void* last_round_result = NULL;

const void* aggregate_program(const AggregateLibrary* lang);

bool on_result(const void* result) {
    static int round = 0;
    last_round_result = result;
    sleep(1);
    return ++round < ITERATIONS;
}

int main(void) {
    DEVICE_ID = codable_int({{ deviceId }});
    Neighborhood neighbors = Neighborhood_empty();
    {{ neighbors }}
    ConnectionOrientedNetworkManager network = socket_network(DEVICE_ID, {{ port }}, neighbors);
    engine(network, aggregate_program, on_result);
    printf("%s", field_to_str((const Field*) last_round_result));
    return 0;
}
