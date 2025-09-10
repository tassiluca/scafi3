#ifndef SCAFI3_FIELD_H
#define SCAFI3_FIELD_H

#include <uthash.h>

typedef struct {
    const void const* id;
    const void const* value;
    UT_hash_handle hh;
} NeighborValue;

typedef struct {
    const void const* default_value;
    const NeighborValue const* neighbor_values;
} Field;

typedef struct {
    const Field const* (*of)(const void const* default_value);
} FieldFactory;

typedef struct {
    const FieldFactory const* field;
} FieldBasedSharedData;

#endif // SCAFI3_FIELD_H
