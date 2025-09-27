#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <unistd.h>
#include "scafi3.h"

// #define NODE1
#define NODE2

#ifdef NODE1
    #define DEVICE_ID 100
    #define OTHER_ID 200
    #define PORT 9000
    #define OTHER_PORT 9001
#else
    #define DEVICE_ID 200
    #define OTHER_ID 100
    #define PORT 9001
    #define OTHER_PORT 9000
#endif

int rounds = 50;

static uint8_t* encode_int(void *data, size_t *encoded_size);
static BinaryCodable* decode_int(const uint8_t *buffer, size_t size);
BinaryCodable make_binary_codable(int value);

void print_neighbor(BinaryCodable* neighbor, BinaryCodable* in) {
    printf("printing neighbor\n");
    printf("  neighbor %d\n", *(int*) neighbor);
    printf("  has value %d\n", *(int*) in);
    printf("  leaving print_neighbor\n");
    fflush(stdout);
}

void print_connections(BinaryCodable* neighbor, struct Endpoint* endpoint) {
    printf("  neighbor %d is at %s:%d\n", *(int*) neighbor->data, endpoint->address, endpoint->port);
}

bool on_result(const void* result) {
    printf("on_result called!\n");
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
    BinaryCodable bin_device_id = make_binary_codable(DEVICE_ID);
    struct Endpoint device_endpoint = { "localhost", PORT };

    BinaryCodable bin_other_id = make_binary_codable(OTHER_ID);
    struct Endpoint other_endpoint = { "localhost", OTHER_PORT };

    Connections endpoints = Connections_empty();
    endpoints = Connections_put(endpoints, &bin_other_id, &other_endpoint);
    endpoints = Connections_put(endpoints, &bin_device_id, &device_endpoint);
    printf("local id %p\n", (void*) &bin_device_id);
    printf("other id %p\n", (void*) &bin_other_id);
    Connections_foreach(endpoints, print_connections);

    ConnectionOrientedNetworkManager network = socket_network(&bin_device_id, PORT, endpoints);
    engine(&bin_device_id, network, aggregate_program, on_result);
    printf("OK!\n");
    return 0;
}

// ------------------------------- SERIALIZATION -------------------------------

static uint8_t* encode_int(void *data, size_t *encoded_size) {
    if (!data || !encoded_size) return NULL;
    *encoded_size = sizeof(int);
    uint8_t *buffer = malloc(*encoded_size);
    if (!buffer) return NULL;
    memcpy(buffer, data, sizeof(int)); // Copy the integer bytes into the buffer (host endianness)
    return buffer;
}

static BinaryCodable* decode_int(const uint8_t *buffer, size_t size) {
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

BinaryCodable make_binary_codable(int value) {
    int *stored_value = malloc(sizeof(int));
    if (!stored_value) {
        fprintf(stderr, "Memory allocation failed\n");
        exit(EXIT_FAILURE);
    }
    *stored_value = value;
    BinaryCodable bc = {
        .data = stored_value,
        .type_name = "int",
        .encode = encode_int,
        .decode = decode_int
    };
    return bc;
}

/*

    const char* message = (const char*) result;
    printf("Result: message is %s\n", message);
    free((void*) message);
    // const BinaryCodable* local_id = (const BinaryCodable*) result;
    // printf("Result: local id is %d\n", *(int*) local_id->data);

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

*/
