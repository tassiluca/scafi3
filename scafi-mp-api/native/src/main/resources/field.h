#ifndef SCAFI3_FIELD_H
#define SCAFI3_FIELD_H

#include <stdint.h>
#include <stddef.h>

// TODO: hash and equality functions?
typedef struct Serializable {
    void *data;
    size_t (*serialize)(void *data, uint8_t *buffer);
    void *(*deserialize)(const uint8_t *buffer, size_t size);
} Serializable;

typedef struct Neighborhood Neighborhood;

Serializable* neighborhood_get(const Neighborhood* neighborhood, const Serializable* key);

typedef struct {
    const Serializable* default_value;
    const Neighborhood* neighbor_values;
} SharedData;

char* shared_data_to_string(const SharedData* field);

typedef struct {
    const SharedData const* (*of)(const void const* default_value);
} FieldBasedSharedData;

#endif // SCAFI3_FIELD_H
