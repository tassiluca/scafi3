#include <stdio.h>
#include <stdlib.h>
#include "test.h"

MAP_OF(ints, int, int)

int main(void) {
    Map m = ints_empty();
    int k1 = 1;
    int v1 = 10;
    int k2 = 2;
    int v2 = 20;
    m = ints_put(m, &k1, &v1);
    m = ints_put(m, &k2, &v2);
    int* val1 = ints_get(m, &k1);
    int* val2 = ints_get(m, &k2);
    printf("Key: %d, Value: %d\n", k1, *val1);
    printf("Key: %d, Value: %d\n", k2, *val2);
    // overwrite value for k1
    int v1_new = 15;
    m = ints_put(m, &k1, &v1_new);
    val1 = ints_get(m, &k1);
    printf("Key: %d, New Value: %d\n", k1, *val1);
    // remove k2
    m = ints_remove(m, &k2);
    val2 = ints_get(m, &k2);
    if (val2 == NULL) {
        printf("Key: %d has been removed.\n", k2);
    } else {
        printf("Key: %d, Value: %d\n", k2, *val2);
    }
    return 0;
}
