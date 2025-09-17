#ifndef RUNTIME_H
#define RUNTIME_H

#include "field.h"

typedef struct ConnectionOrientedNetworkManager ConnectionOrientedNetworkManager;

ConnectionOrientedNetworkManager* socket_network(const Serializable* device_id);

typedef struct AggregateLibrary AggregateLibrary;

void engine(
    const Serializable* device_id,
    const ConnectionOrientedNetworkManager* network_manager,
    const void* (*aggregate_program)(const AggregateLibrary* library),
    bool (*on_result)(const void* result)
);

#endif // RUNTIME_H
