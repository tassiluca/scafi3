########################################################################################################################
# Makefile for building the C integration test with the ScaFi3 native library.
########################################################################################################################

# Detect OS
ifeq ($(OS),Windows_NT)
	EXE = main.exe
	LIB_EXT = dll
	RPATH =
	CC = gcc
	LIBLINK = scafi3.lib # MSVC linker looks for scafi3.lib when you use -lscafi3
else ifeq ($(shell uname -s),Darwin)
	EXE = main
	LIB_EXT = dylib
	RPATH = -Wl,-rpath,.
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
	LIBLINK = -L. -lscafi3 $(RPATH)
else
	EXE = main
	LIB_EXT = so
	RPATH = -Wl,-rpath,.
	CC = gcc
	LIBLINK = -L. -lscafi3 $(RPATH)
endif

CFLAGS = -std=gnu11 -Wall -Wextra -Werror

all: debug $(EXE)

debug:
	@echo "Building executable for $(EXE)"
	@echo "Current directory:"
	@cd
	@echo "Files in current directory:"
ifeq ($(OS),Windows_NT)
	@dir *.lib *.dll *.a 2>nul || echo "No library files found"
else
	@ls -la
endif
	@echo "Compiler: $(CC)"
	@echo "Flags: $(CFLAGS)"

$(EXE): main.c
	$(CC) $(CFLAGS) main.c -I. $(LIBLINK) -o $(EXE)

clean:
	rm -f $(EXE)

.PHONY: all clean
