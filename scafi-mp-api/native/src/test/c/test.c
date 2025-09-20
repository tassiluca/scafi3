#include <stdio.h>
#include <stdlib.h>
#include "scafi3.h"

bool termination_criteria(const void* result) {
    return true;
}

int main(void) {
    int deviceId = 1;
    const ConnectionOrientedNetworkManager* network = socket_network(&deviceId, 9000, Connections_empty());
    engine(&deviceId, network, NULL, termination_criteria);
    printf("OK!\n");
    return 0;
}
