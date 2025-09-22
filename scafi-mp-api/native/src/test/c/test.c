#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "scafi3.h"

bool on_result(const void* result) {
    printf("Result reaction\n");
    return true;
}

const void* aggregate_program(const AggregateLibrary* lib) {
    sleep(1);
    return NULL;
}

int main(void) {
    const ConnectionOrientedNetworkManager* network = socket_network(NULL, 9000, Connections_empty());
    engine(NULL, network, aggregate_program, on_result);
    printf("OK!\n");
    return 0;
}
