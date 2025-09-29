CC = clang
CFLAGS = -Wall -Wextra -Werror

# Detect OS
ifeq ($(OS),Windows_NT)
    EXE = main.exe
else
    EXE = main
endif

all: $(EXE)

$(EXE): main.c
	$(CC) $(CFLAGS) -o $(EXE) main.c
