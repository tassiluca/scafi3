#ifndef LIBRARIES_H
#define LIBRARIES_H

#include "field.h"

typedef struct AggregateLibrary {
    FieldBasedSharedData Field;
    struct { // Common library
        BinaryCodable* (*local_id)(void);
        SharedData* (*device_id)(void);
    };
} AggregateLibrary;

#endif // LIBRARIES_H