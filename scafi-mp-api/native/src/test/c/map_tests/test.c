#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "internals.h"
#include "utils.h"

void print_kv(const void* key, const void* value) {
    int k = *(int*)key;
    int v = *(int*)value;
    printf("key %d has value %d\n", k, v);
}

void print_kv_typed(const int* key, const int* value) {
    printf("key %d has value %d\n", *key, *value);
}

MAP_OF(IntIntMap, int, int, int_compare)

int main(void) {
    Map map = map_empty(int_compare);
    int k1 = 1;
    int v1 = 10;
    int k2 = 1;
    int v2 = 20;

    map_put(map, &k1, &v1); // 1 -> 10
    int* val = (int*) map_get(map, &k1);
    printf("key %d has value %d\n", k1, *val);

    val = (int*) map_get(map, &k2); // k2 == k1 so should find it => 10
    if (val) {
        printf("key %d has value %d\n", k2, *val);
    } else {
        printf("key %d not found\n", k2);
    }

    map_put(map, &k2, &v2);
    printf("After updating key k2\n");

    val = (int*) map_get(map, &k2);
    printf("key %d has value %d\n", k2, *val);
    val = (int*) map_get(map, &k1);
    printf("key %d has value %d\n", k1, *val);

    int k3 = 3;
    int v3 = 30;
    map_put(map, &k3, &v3);

    printf("Iterating over map:\n");
    map_foreach(map, print_kv);

    int k4 = 4;
    int v4 = 4000;
    map_put(map, &k4, &v4);
    map_put(map, &k3, &v4); // update

    printf("Iterating over map:\n");
    map_foreach(map, print_kv);

    map_free(map);

    // WITH MACRO FACADE TYPE-SAFE USAGE
    printf("====================================\n");
    IntIntMap imap = IntIntMap_empty();
    IntIntMap_put(imap, &k1, &v1); // 1 -> 10
    int* ival = IntIntMap_get(imap, &k1);
    printf("key %d has value %d\n", k1, *ival);

    IntIntMap_put(imap, &k2, &v2);
    ival = IntIntMap_get(imap, &k2); // k2 == k1 so should find it => 10
    printf("key %d has value %d\n", k2, *ival);
    printf("key %d has value %d\n", k1, *IntIntMap_get(imap, &k1));

    IntIntMap_put(imap, &k3, &v3);
    IntIntMap_put(imap, &k4, &v4);
    printf("Iterating over map:\n");
    IntIntMap_foreach(imap, print_kv_typed);
    
    IntIntMap_put(imap, &k3, &v4); // update
    printf("Iterating over map:\n");
    IntIntMap_foreach(imap, print_kv_typed);
    IntIntMap_free(imap);

    printf("OK!\n");
    return 0;
}
