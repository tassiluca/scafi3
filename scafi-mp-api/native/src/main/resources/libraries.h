#ifndef LIBRARIES_H
#define LIBRARIES_H

#include <stdbool.h>
#include "field.h"

typedef struct ReturnSending* ReturnSending;

typedef struct AggregateLibrary {
    FieldBasedSharedData Field;
    struct { // Common library
        BinaryCodable* (*local_id)(void);
        SharedData* (*device)(void);
    };
    struct { // Branching library
        void* (*branch)(bool condition, void* (*true_branch)(void), void* (*false_branch)(void));
    };
    struct { // Exchange library
        SharedData* (*exchange)(const SharedData* initial, const ReturnSending* (*f)(SharedData* in));
    };
} AggregateLibrary;

#endif // LIBRARIES_H