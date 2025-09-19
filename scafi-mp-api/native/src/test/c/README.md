# Tests

To compile manually:

```bash
clang test.c -I. -L. -lscafi-mp-api -o test -Wl,-rpath,.
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
