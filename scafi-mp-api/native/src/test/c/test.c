#include <stdio.h>
#include <stdlib.h>
#include "test.h"

int main(void) {
    Tuple2* t1 = pair((void*)1, (void*)2);
    printf("(%d, %d)\n", (int)(long)fst(t1), (int)(long)snd(t1));

    Tuple2* t2 = pair((void*)3, (void*)4);
    printf("(%d, %d)\n", (int)(long)fst(t2), (int)(long)snd(t2));

    Map m = map_empty();
    Map m1 = map_put(m, (void*)1, (void*)2);
    Map m2 = map_put(m1, (void*)3, (void*)4);
    Map m3 = map_put(m2, (void*)10, (void*)20);
    Map m4 = map_put(m3, (void*)1, (void*)3); // overwrite? yes

    printf("map_get 1: %d\n", (int)(long)map_get(m4, (void*)1));
    printf("map_get 3: %d\n", (int)(long)map_get(m4, (void*)3));
    printf("map_get 10: %d\n", (int)(long)map_get(m4, (void*)10));

    Map m5 = map_of((Tuple2*[]){t1, t2}, 2);
    printf("map_of get 1: %d\n", (int)(long)map_get(m5, (void*)1));
    printf("map_of get 3: %d\n", (int)(long)map_get(m5, (void*)3));

    return 0;
}
