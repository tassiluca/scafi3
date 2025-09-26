#ifndef SCAFI3_FIELD_H
#define SCAFI3_FIELD_H

#include <inttypes.h>
#include <stddef.h>
#include "utils.h"

// TODO: hash and equality functions?
typedef struct BinaryCodable {
    void *data;
    char* type_name;
    size_t (*encode)(void *data, uint8_t *buffer);
    void* (*decode)(const uint8_t *buffer, size_t size);
} BinaryCodable;

MAP_OF(Neighborhood, BinaryCodable, BinaryCodable)

typedef struct {
    const BinaryCodable* default_value;
    const Neighborhood neighbor_values;
} SharedData;

char* shared_data_to_string(const SharedData* field);

typedef struct {
    const SharedData* (*of)(const BinaryCodable* default_value);
} FieldBasedSharedData;

#endif // SCAFI3_FIELD_H
