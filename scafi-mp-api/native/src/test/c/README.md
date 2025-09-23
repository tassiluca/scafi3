# Tests

To compile manually:

```bash
clang test.c -I. -L. -lscafi-mp-api -o test -Wl,-rpath,.
```

Complete:

```bash
rm -rf ./test && rm -rf libscafi-mp-api.dylib && cp ../../../target/scala-3.7.2/libscafi-mp-api.dylib . && clang test.c -I. -L. -lscafi-mp-api -o test -Wl,-rpath,. && ./test
```

---

```bash
clang test.c \
  -I. -L. -lscafi-mp-api \
  -o test -Wl,-rpath,. \
  -Wall -Wextra -Wpedantic \
  -Wshadow -Wformat=2 -Wconversion -Wsign-conversion \
  -Wnull-dereference \
  -O1 -g -fstack-protector-strong -D_FORTIFY_SOURCE=2 \
  -fPIE -pie \
  -fsanitize=address,undefined \
  -fno-omit-frame-pointer
```

on not macos also add `-fsanitize=leak` and `-fsanitize=thread`.
