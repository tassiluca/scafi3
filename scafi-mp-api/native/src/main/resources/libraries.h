#ifndef LIBRARIES_H
#define LIBRARIES_H

#include "field.h"

typedef struct CommonLibrary {
    BinaryCodable* (*local_id)(void);
    SharedData* (*device_id)(void);
} CommonLibrary;

typedef struct AggregateLibrary {
    FieldBasedSharedData* Field;
    CommonLibrary* common;
} AggregateLibrary;

#endif // LIBRARIES_H