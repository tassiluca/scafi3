#ifndef RUNTIME_H
#define RUNTIME_H

#include <stdbool.h>
#include "libraries.h"

typedef struct ConnectionOrientedNetworkManagerImpl* ConnectionOrientedNetworkManager;

struct Endpoint {
    char* address;
    int port;
};

MAP_OF(Neighborhood, BinaryCodable, struct Endpoint, DEVICE_ID->equals, DEVICE_ID->hash)

ConnectionOrientedNetworkManager socket_network(
    const BinaryCodable* device_id,
    int port, 
    const Neighborhood neighbors
);

void engine(
    const ConnectionOrientedNetworkManager network_manager,
    const void* (*aggregate_program)(const AggregateLibrary* library),
    bool (*on_result)(const void* result)
);

#endif // RUNTIME_H
