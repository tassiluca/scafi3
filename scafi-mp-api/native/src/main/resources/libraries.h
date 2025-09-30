#ifndef LIBRARIES_H
#define LIBRARIES_H

#include <stdbool.h>
#include "field.h"

typedef struct ReturnSendingImpl* ReturnSending;

ReturnSending retsend(const SharedData* value);

ReturnSending return_sending(const SharedData* returning, const SharedData* send);

typedef struct AggregateLibrary {
    FieldBasedSharedData Field;
    struct { // Common library
        const BinaryCodable* (*local_id)(void);
        const SharedData* (*device)(void);
    };
    struct { // Branching library
        const void* (*branch)(bool condition, const void* (*true_branch)(void), const void* (*false_branch)(void));
    };
    struct { // Exchange library
        const SharedData* (*exchange)(const SharedData* initial, ReturnSending (*f)(const SharedData* in));
    };
} AggregateLibrary;

#endif // LIBRARIES_H
