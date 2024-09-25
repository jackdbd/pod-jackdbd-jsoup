#!/usr/bin/env bash
set -euo pipefail

POD_ID=pod.jackdbd.jsoup
POD_NAME=pod-jackdbd-jsoup
POD_VERSION=0.1.0
UBERJAR_PATH="target/$POD_ID-$POD_VERSION-standalone.jar"

# In the GraalVM Community Edition, only the Serial GC is available.
# https://www.graalvm.org/22.0/reference-manual/native-image/MemoryManagement/
# If no maximum Java heap size is specified, a native image that uses the Serial
# GC will set its maximum Java heap size to 80% of the physical memory size.
# https://www.graalvm.org/22.0/reference-manual/native-image/MemoryManagement/#java-heap-size
JVM_MAX_HEAP_SIZE_AT_RUN_TIME="-Xmx4500m"
HEAP_SIZE_AT_BUILD_TIME="-R:MaxHeapSize=1024m"

# Optimization levels and CPU features
# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/
# -march=native -Ob

# I think cross-compiling the GraalVM native image requires downloading the C
# library for that OS-architecture and place it here:
# RUN ls /usr/lib64/graalvm/graalvm22-ce-java17/lib/svm/clibraries
# See also how Babashka does it:
# https://github.com/babashka/babashka/blob/master/.github/workflows/build.yml
TARGET="linux-amd64"

# https://www.graalvm.org/22.1/reference-manual/native-image/StaticImages/
# Use these 2 options to compile a mostly statically-linked binary
# "-H:+StaticExecutableWithDynamicLibC"
# "--libc=glibc"
# Use these 2 options to compile a completely statically-linked binary
# "--static"
# "--libc=musl"

echo "Compile $UBERJAR_PATH"
echo "BABASHKA_STATIC=$BABASHKA_STATIC"
echo "BABASHKA_MUSL=$BABASHKA_MUSL"
echo "Use the following GraalVM native-image"
native-image --version

# https://github.com/babashka/pod-babashka-parcera/blob/master/script/compile
# https://github.com/jaydeesimon/pod-jaydeesimon-jsoup/blob/master/script/compile

# This works, when using the environment variables CPATH, LIBRARY_PATH, NIX_LDFLAGS
native-image -jar $UBERJAR_PATH \
  -H:ReflectionConfigurationFiles=reflection.json \
  -H:+ReportExceptionStackTraces \
  -J-Dclojure.compiler.direct-linking=true \
  -J-Dclojure.spec.skip-macros=true \
  "-J$JVM_MAX_HEAP_SIZE_AT_RUN_TIME" \
  $HEAP_SIZE_AT_BUILD_TIME \
  -march=native -Ob \
  --gc=serial \
  --initialize-at-build-time \
  --native-image-info \
  --no-fallback \
  --no-server \
  --report-unsupported-elements-at-runtime \
  --static --libc=musl \
  "--target=$TARGET" \
  --verbose

mv "$POD_ID-$POD_VERSION-standalone" "target/$POD_NAME-$POD_VERSION"
echo "Binary artifact moved to target/$POD_NAME-$POD_VERSION"
