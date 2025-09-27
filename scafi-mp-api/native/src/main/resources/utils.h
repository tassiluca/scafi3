#ifndef UTILS_H
#define UTILS_H

#include "internals.h"

#define PAIR_OF(NAME, FIRST_TYPE, SECOND_TYPE)                                  \
    typedef Tuple2 NAME;                                                        \
    static inline Tuple2 NAME(FIRST_TYPE* first, SECOND_TYPE* second) {         \
        return pair((void*)first, (void*)second);                               \
    }                                                                           \
    static inline FIRST_TYPE* NAME##_fst(const Tuple2 tuple) {                  \
        return (FIRST_TYPE*)fst(tuple);                                         \
    }                                                                           \
    static inline SECOND_TYPE* NAME##_snd(const Tuple2 tuple) {                 \
        return (SECOND_TYPE*)snd(tuple);                                        \
    }

#define MAP_OF(NAME, KEYS_TYPE, VALUES_TYPE)                                    \
    typedef Map NAME;                                                           \
    static inline NAME NAME##_empty(void) {                                     \
        return map_empty();                                                     \
    }                                                                           \
    static inline Map NAME##_put(Map map, KEYS_TYPE* key, VALUES_TYPE* value) { \
        return map_put(map, (void*)key, (void*)value);                          \
    }                                                                           \
    static inline VALUES_TYPE* NAME##_get(Map map, KEYS_TYPE* key) {            \
        return (VALUES_TYPE*)map_get(map, (void*)key);                          \
    }                                                                           \
    static inline Map NAME##_remove(Map map, KEYS_TYPE* key) {                  \
        return map_remove(map, (void*)key);                                     \
    }                                                                           \
    static inline void NAME##_foreach(Map map, void (*f)(KEYS_TYPE* key, VALUES_TYPE* value)) { \
        map_foreach(map, (void (*)(void*, void*))f);                          \
    }                                                                           \
    static inline Map NAME##_of(Tuple2* entries, size_t size) {                 \
        Map map = map_empty();                                                  \
        for (size_t i = 0; i < size; i++) {                                     \
            map = map_put(map, fst(entries[i]), snd(entries[i]));               \
        }                                                                       \
        return map;                                                             \
    }

#endif // UTILS_H
