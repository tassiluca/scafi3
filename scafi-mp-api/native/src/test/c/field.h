#ifndef SCAFI3_FIELD_H
#define SCAFI3_FIELD_H

#include <stdint.h>
#include <stddef.h>
#include "utils.h"

// TODO: hash and equality functions?
typedef struct Serializable {
    void *data;
    size_t (*serialize)(void *data, uint8_t *buffer);
    void* (*deserialize)(const uint8_t *buffer, size_t size);
} Serializable;

MAP_OF(Neighborhood, Serializable, Serializable)

typedef struct {
    const Serializable* default_value;
    const Neighborhood* neighbor_values;
} SharedData;

char* shared_data_to_string(const SharedData* field);

typedef struct {
    const SharedData* (*of)(const void* default_value);
} FieldBasedSharedData;

#endif // SCAFI3_FIELD_H
