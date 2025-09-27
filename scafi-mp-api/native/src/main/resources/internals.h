#ifndef INTERNALS_H
#define INTERNALS_H

typedef struct Tuple2Impl* Tuple2;

Tuple2 pair(void* first, void* second);
void* fst(const Tuple2 tuple);
void* snd(const Tuple2 tuple);

typedef struct MapImpl* Map;

Map map_empty();
Map map_put(Map map, void* key, void* value);
Map map_remove(Map map, void* key);
void* map_get(const Map map, void* key);
void map_foreach(const Map map, void (*f)(void* key, void* value));

#endif // INTERNALS_H
