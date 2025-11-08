#ifndef LIBRARIES_H
#define LIBRARIES_H

#include <stdbool.h>
#include "field.h"

typedef struct AggregateLibrary {
    FieldBasedSharedData Field;
    const BinaryCodable* (*local_id)(void);
    const void* (*branch)(bool condition, const void* (*true_branch)(void), const void* (*false_branch)(void));
    const void* (*evolve)(const void* initial, const void* (*evolution)(const void*));
    const BinaryCodable* (*share)(const BinaryCodable* initial, const BinaryCodable* (*f)(const Field* nvalues));
    const Field* (*neighbor_values)(const BinaryCodable* value);
} AggregateLibrary;

#endif // LIBRARIES_H
