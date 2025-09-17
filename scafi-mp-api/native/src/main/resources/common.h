#ifndef COMMON_SCAFI3_LIB_H
#define COMMON_SCAFI3_LIB_H

#include "field.h"

typedef struct {
    FieldBasedSharedData Field;
    Serializable* (*local_id)(void);
    SharedData* (*device_id)(void);
} CommonLibrary;

#endif // COMMON_SCAFI3_LIB_H
