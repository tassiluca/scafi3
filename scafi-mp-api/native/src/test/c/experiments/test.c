#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

// --- Struct definitions ---

typedef struct Person {
    char *name;
    int age;
} Person;

typedef struct {
    const Person* (*of)(const char* name);
} Foo;

typedef struct FullLibrary {
    Foo Person;
    struct { // "Common" library
        char* (*today)(void);
    };
} FullLibrary;

// --- Implementation functions ---

// Creates a new Person dynamically
const Person* create_person(const char* name) {
    Person* p = malloc(sizeof(Person));
    if (!p) {
        fprintf(stderr, "Memory allocation failed!\n");
        exit(EXIT_FAILURE);
    }

    // allocate and copy the name
    p->name = malloc(strlen(name) + 1);
    if (!p->name) {
        fprintf(stderr, "Memory allocation failed!\n");
        free(p);
        exit(EXIT_FAILURE);
    }
    strcpy(p->name, name);

    // assign a random age between 18 and 60
    p->age = 18 + rand() % 43;

    return p;
}

// Implementation of Common.today
char* get_today_string(void) {
    static char buffer[11]; // YYYY-MM-DD + null
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    snprintf(buffer, sizeof(buffer), "%04d-%02d-%02d",
             t->tm_year + 1900, t->tm_mon + 1, t->tm_mday);
    return buffer;
}

// --- Putting it all together ---

FullLibrary* create_full_library(void) {
    FullLibrary* lib = malloc(sizeof(FullLibrary));
    if (!lib) {
        fprintf(stderr, "Memory allocation failed!\n");
        exit(EXIT_FAILURE);
    }

    lib->Person.of = create_person;
    lib->today = get_today_string;

    return lib;
}

// --- Test code ---

int main(void) {
    srand((unsigned)time(NULL)); // seed random number generator

    FullLibrary* lib = create_full_library();

    const Person *p1 = lib->Person.of("Alice");
    const Person *p2 = lib->Person.of("Bob");

    printf("Created person: %s, age %d\n", p1->name, p1->age);
    printf("Created person: %s, age %d\n", p2->name, p2->age);

    printf("Today is %s\n", lib->today());

    // Free memory
    free((void*)p1->name);
    free((void*)p1);
    free((void*)p2->name);
    free((void*)p2);
    free(lib);

    return 0;
}
