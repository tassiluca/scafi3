#ifndef RUNTIME_H
#define RUNTIME_H

#include <stdbool.h>
#include "libraries.h"

typedef struct ConnectionOrientedNetworkManager ConnectionOrientedNetworkManager;

struct Endpoint {
    char* address;
    uint16_t port;
};

MAP_OF(Connections, BinaryCodable, struct Endpoint)

ConnectionOrientedNetworkManager* socket_network(
    const BinaryCodable* device_id,
    int port, 
    const Connections neighbors
);

void engine(
    const BinaryCodable* device_id,
    const ConnectionOrientedNetworkManager* network_manager,
    const void* (*aggregate_program)(const AggregateLibrary* library),
    bool (*on_result)(const void* result)
);

#endif // RUNTIME_H
