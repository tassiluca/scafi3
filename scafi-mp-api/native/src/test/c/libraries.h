#ifndef LIBRARIES_H
#define LIBRARIES_H

#include "field.h"

typedef struct AggregateLibrary {
    FieldBasedSharedData Field;
    struct { // Common library
        BinaryCodable* (*local_id)(void);
        SharedData* (*device)(void);
    };
    struct { // Branching library
        void* (*branch)(bool condition, void* (*true_branch)(void), void* (*false_branch)(void));
    };
} AggregateLibrary;

#endif // LIBRARIES_H