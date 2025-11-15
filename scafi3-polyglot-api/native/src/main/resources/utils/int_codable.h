#ifndef INT_CODABLE_H
#define INT_CODABLE_H

#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#include <message.h>

/**
 * A structure representing an integer value as a binary codable.
 */
typedef struct Int {
    BinaryCodable base;
    int value;
} Int;

/**
 * Creates a new `Int` instance from the given integer value.
 * @param value the integer value
 * @return a pointer to the newly created `Int` instance
 */
BinaryCodable* int_of(int value);

#endif