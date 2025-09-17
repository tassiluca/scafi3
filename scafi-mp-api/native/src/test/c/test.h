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

Map map_of(Tuple2** entries, size_t size) {
    Map map = map_empty();
    for (size_t i = 0; i < size; i++) {
        map = map_put(map, fst(entries[i]), snd(entries[i]));
    }
    return map;
}

void* map_get(const Map map, void* key);

#endif // TEST_H