#ifndef SCAFI3_FIELD_H
#define SCAFI3_FIELD_H

#include <inttypes.h>
#include <stddef.h>
#include "utils.h"

typedef struct BinaryCodable {
    void *data;
    char* type_name;
    uint8_t* (*encode)(void *data, size_t *encoded_size);
    struct BinaryCodable* (*decode)(const uint8_t *buffer, size_t size);
    bool (*equals)(const void* a, const void* b);
    uint32_t (*hash)(const void* data);
    char* (*to_str)(const void* data);
} BinaryCodable;

extern BinaryCodable* DEVICE_ID;

MAP_OF(NValues, BinaryCodable, BinaryCodable, DEVICE_ID->equals, DEVICE_ID->hash)

typedef struct {
    const BinaryCodable* default_value;
    const NValues neighbor_values;
} Field;

/**
 * Converts a Field instance to a string representation.
 * @param field The Field instance to convert.
 * @return char* A string representation of the Field instance.
 * @note The returned string must be freed by the caller when no longer needed.
 */
char* field_to_str(const Field* field);

/**
 * Field-based shared data factory.
 */
typedef struct {
    /**
     * Creates a new field-based shared data instance.
     * @param default_value The default value for the field.
     * @return A pointer to the newly created Field instance.
     * @note The returned instance must be freed by the caller when no longer needed.
     */
    const Field* (*of)(const BinaryCodable* default_value);
} FieldBasedSharedData;

#endif // SCAFI3_FIELD_H
