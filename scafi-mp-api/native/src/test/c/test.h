#ifndef TEST_H
#define TEST_H

#include <stdarg.h>
#include <stddef.h>

typedef struct Tuple2 Tuple2;

Tuple2* pair(void* first, void* second);

void* fst(const Tuple2* tuple);

void* snd(const Tuple2* tuple);

typedef struct Map* Map;

Map map_empty();

Map map_put(Map map, void* key, void* value);

void* map_get(const Map map, void* key);

#endif // TEST_H