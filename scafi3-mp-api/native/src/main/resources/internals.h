#ifndef INTERNALS_H
#define INTERNALS_H

#include <stdlib.h>
#include <stdbool.h>

typedef struct MapImpl* Map;

Map map_empty(bool (*equals)(const void*, const void*), uint32_t (*hash)(const void*));
void map_put(Map map, const void* key, const void* value);
void* map_get(const Map map, const void* key);
size_t map_size(const Map map);
void map_foreach(const Map map, void (*f)(const void* key, const void* value));
void map_free(Map map);

#endif // INTERNALS_H
