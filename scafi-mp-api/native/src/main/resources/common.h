#ifndef COMMON_SCAFI3_LIB_H
#define COMMON_SCAFI3_LIB_H

#include "field.h"

typedef struct {
    FieldBasedSharedData Field;
    Serializable* (*local_id)();
    SharedData* (*device_id)();
} CommonLibrary;

#endif // COMMON_SCAFI3_LIB_H
