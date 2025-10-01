########################################################################################################################
# Makefile for building the C integration test with the ScaFi3 native library.
########################################################################################################################

ifeq ($(OS),Windows_NT)
	EXE = main.exe
	CC = gcc
	SCAFI3_LIB = scafi3.dll scafi3.lib
	LIBLINK = scafi3.lib
else ifeq ($(shell uname -s),Darwin)
	EXE = main
    # On MacOS, usually gcc is an alias to clang, so we try to find a real GCC installation
	CC := $(shell \
		if gcc --version 2>/dev/null | grep -q "^gcc"; then \
			echo gcc; \
		else \
			find /opt/homebrew/bin /usr/local/bin -name 'gcc-[0-9]*' 2>/dev/null | sort -V | tail -1 || \
			echo "NOT_FOUND"; \
		fi)
	ifeq ($(CC),NOT_FOUND)
		$(error GCC not found. Install: brew install gcc)
	endif
	SCAFI3_LIB = libscafi3.dylib
	LIBLINK = -L. -Wl,-rpath,. -lscafi3
else
	EXE = main
	CC = gcc
	SCAFI3_LIB = libscafi3.so
	LIBLINK = -L. -Wl,-rpath,. -lscafi3
endif

CFLAGS = -std=gnu11 -Wall -Wextra -Werror

all: $(EXE)

$(EXE): main.c $(SCAFI3_LIB)
	$(CC) $(CFLAGS) main.c -I. $(LIBLINK) -o $(EXE)

clean:
	rm -f $(EXE)

.PHONY: all clean
