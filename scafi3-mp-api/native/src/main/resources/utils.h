#ifndef UTILS_H
#define UTILS_H

/******************************************************************************************************************
 * Control structures
 *****************************************************************************************************************/
#include "internals.h"

#define lambda(ret, args, body) \
    ({ \
    ret __fn__ args body \
    &__fn__; \
    })

#define fn lambda

#define MAP_OF(Name, KeysType, ValuesType, CompareFunc, HashFunc)                           \
    typedef Map Name;                                                                       \
    static inline Name Name##_empty() {                                                     \
        return map_empty(CompareFunc, HashFunc);                                            \
    }                                                                                       \
    static inline void Name##_put(Name map, const KeysType* key, const ValuesType* value) { \
        map_put(map, key, value);                                                           \
    }                                                                                       \
    static inline ValuesType* Name##_get(const Name map, const KeysType* key) {             \
        return (ValuesType*)map_get(map, key);                                              \
    }                                                                                       \
    static inline size_t Name##_size(const Name map) {                                      \
        return map_size(map);                                                               \
    }                                                                                       \
    static inline void Name##_foreach(const Name map, void (*f)(const KeysType* key, const ValuesType* value)) { \
        map_foreach(map, (void (*)(const void*, const void*))f);                            \
    }                                                                                       \
    static inline void Name##_free(Name map) {                                              \
        map_free(map);                                                                      \
    }

#endif // UTILS_H
