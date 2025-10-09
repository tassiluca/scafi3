#ifndef LIBRARIES_H
#define LIBRARIES_H

#include <stdbool.h>
#include "field.h"

typedef struct ReturnSendingImpl* ReturnSending;

ReturnSending retsend(const Field* value);

ReturnSending return_sending(const Field* returning, const Field* send);

typedef struct AggregateLibrary {
    FieldBasedSharedData Field;
    struct { // Common library
        const BinaryCodable* (*local_id)(void);
        const Field* (*device)(void);
    };
    struct { // Branching library
        const void* (*branch)(bool condition, const void* (*true_branch)(void), const void* (*false_branch)(void));
    };
    struct { // Exchange library
        const Field* (*exchange)(const Field* initial, ReturnSending (*f)(const Field* in));
    };
} AggregateLibrary;

#endif // LIBRARIES_H
