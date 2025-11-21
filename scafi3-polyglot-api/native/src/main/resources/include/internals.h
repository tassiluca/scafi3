#ifndef INTERNALS_H
#define INTERNALS_H

#include <stdlib.h>
#include <stdbool.h>
#include "message.h"

typedef struct MapImpl* Map;

Map map_empty();
void* map_put(Map map, const void* key, const void* value);
void* map_int_put(Map map, int key, const void* value);
void* map_get(const Map map, const void* key);
void* map_int_get(const Map map, int key);
void map_foreach(const Map map, void (*f)(const void* value));
size_t map_size(const Map map);
void map_free(Map map);

#endif // INTERNALS_H
