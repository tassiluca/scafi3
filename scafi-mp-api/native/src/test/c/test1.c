#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <unistd.h>
#include "scafi3.h"

#define NODE1

#ifdef NODE1
    #define LOCAL_ID 100
    #define OTHER_ID 200
    #define PORT 9000
    #define OTHER_PORT 9001
#else
    #define LOCAL_ID 200
    #define OTHER_ID 100
    #define PORT 9001
    #define OTHER_PORT 9000
#endif

int local_device_id = LOCAL_ID;
int other_device_id = OTHER_ID;

// ------------------------------- SERIALIZATION -------------------------------

static uint8_t* encode_int(void *data, size_t *encoded_size) {
    if (!data || !encoded_size) return NULL;
    *encoded_size = sizeof(int);
    uint8_t *buffer = malloc(*encoded_size);
    if (!buffer) return NULL;
    memcpy(buffer, data, sizeof(int)); // Copy the integer bytes into the buffer (host endianness)
    return buffer;
}

static void* decode_int(const uint8_t *buffer, size_t size) {
    if (!buffer || size != sizeof(int)) return NULL;
    // First decode the integer value
    int decoded_value;
    memcpy(&decoded_value, buffer, sizeof(int));
    // Create a new BinaryCodable with the decoded value
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
    return bc;
}

const BinaryCodable DEVICE_ID = {
    .data = &local_device_id,
    .type_name = "int",
    .encode = encode_int,
    .decode = decode_int,
    .are_equals = int_compare
};

const BinaryCodable OTHER_DEVICE_ID = {
    .data = &other_device_id,
    .type_name = "int",
    .encode = encode_int,
    .decode = decode_int,
    .are_equals = int_compare
};

// ------------------------------- TEST PROGRAM -------------------------------

int rounds = 50;

void print_neighbor(const BinaryCodable* neighbor, const BinaryCodable* in) {
    printf("printing neighbor\n");
    printf("  neighbor %d\n", *(int*) neighbor->data);
    printf("  has value %d\n", *(int*) in->data);
    fflush(stdout);
}

void print_connections(const BinaryCodable* neighbor, const struct Endpoint* endpoint) {
    printf("  neighbor %d is at %s:%d\n", *(int*) neighbor->data, endpoint->address, endpoint->port);
}

bool on_result(const void* result) {
    printf("[%d] on_result called!\n", rounds);
    const SharedData* in = (const SharedData*) result;
    printf("  default value is %d\n", *(int*) in->default_value->data);
    Neighborhood_foreach(in->neighbor_values, print_neighbor);
    printf("====================================\n\n");
    return rounds-- > 0;
}

const ReturnSending on_xc(SharedData* in) {
    printf("on_xc called!\n");
    printf("  default value is %d\n", *(int*) in->default_value->data);
    Neighborhood_foreach(in->neighbor_values, print_neighbor);
    return return_sending(in);
}

const void* aggregate_program(const AggregateLibrary* lib) {
    usleep(500 * 1000);  // sleep 500 ms (500,000 microseconds)
    const SharedData* sd = lib->Field.of(lib->local_id());
    const SharedData* result = lib->exchange(sd, on_xc);
    free((void*) lib); // TODO: manage memory better / automatically
    return result;
}

int main(void) {
    printf("Device %d - ptr. is %p - are equals ptr is %p\n", LOCAL_ID, (void*) &DEVICE_ID, (void*) &DEVICE_ID.are_equals);

    struct Endpoint device_endpoint = { "localhost", PORT };
    struct Endpoint other_endpoint = { "localhost", OTHER_PORT };
    Connections endpoints = Connections_empty();
    Connections_put(endpoints, &OTHER_DEVICE_ID, &other_endpoint);
    Connections_put(endpoints, &DEVICE_ID, &device_endpoint);
    printf("local id %p\n", (void*) &DEVICE_ID);
    printf("other id %p\n", (void*) &OTHER_DEVICE_ID);
    Connections_foreach(endpoints, print_connections);

    ConnectionOrientedNetworkManager network = socket_network(&DEVICE_ID, PORT, endpoints);
    engine(&DEVICE_ID, network, aggregate_program, on_result);
    printf("OK!\n");
    return 0;
}
