#ifndef MESSAGE_H
#define MESSAGE_H

#include <inttypes.h>
#include <stddef.h>

typedef struct BinaryCodable {
    void *data;
    char* type_name;
    uint8_t* (*encode)(void *data, size_t *encoded_size);
    struct BinaryCodable* (*decode)(const uint8_t *buffer, size_t size);
    bool (*equals)(const void* a, const void* b);
    uint32_t (*hash)(const void* data);
    char* (*to_str)(const void* data);
} BinaryCodable;

BinaryCodable* codable_int(int value);

#define codable(x) _Generic((x), \
    int: codable_int             \
)(x)

#endif // MESSAGE_H