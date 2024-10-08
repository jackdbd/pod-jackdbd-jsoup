#!/usr/bin/env bash
set -euo pipefail

# See deps.edn and the pom.xml generated when building the uberjar.
GROUP_ID=com.github.jackdbd
ARTIFACT_ID=pod.jackdbd.jsoup
ARTIFACT_VERSION=$POD_VERSION

# https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#default-environment-variables
if [ "${CI+x}" ]; then
  # running on GitHub actions
  UBERJAR_PATH="$ARTIFACT_ID-$ARTIFACT_VERSION-standalone.jar"
else
  UBERJAR_PATH="target/$ARTIFACT_ID-$ARTIFACT_VERSION-standalone.jar"
fi
echo "UBERJAR_PATH is $UBERJAR_PATH"

# Entry point of the GraalVM native-image documentation.
# https://www.graalvm.org/latest/reference-manual/native-image/
# https://www.graalvm.org/latest/reference-manual/native-image/overview/BuildOutput/

## Memory management ###########################################################

# Epsilon GC is NOT a garbage collector. It NEVER allocates/frees memory.
# GARBAGE_COLLECTOR="--gc=epsilon"
# Serial GC is the only GC available in GraalVM Community Edition.
GARBAGE_COLLECTOR="--gc=serial"

# If the Java heap is full and the GC is unable reclaim sufficient memory for a
# Java object allocation, the allocation will fail with the OutOfMemoryError.
# HEAP_SIZE_AT_BUILD_TIME="-R:MaxHeapSize=1m" # 1MB: not enough => OutOfMemoryError
# HEAP_SIZE_AT_BUILD_TIME="-R:MaxHeapSize=64m" # 64MB: ok => no OutOfMemoryError
# HEAP_SIZE_AT_BUILD_TIME="-R:MaxHeapSize=256m" # 256MB: ok => no OutOfMemoryError
# HEAP_SIZE_AT_BUILD_TIME="-R:MaxHeapSize=1024m" # 1GB: ok => no OutOfMemoryError
# We can also set a maximum heap size as a percentage% of the physical memory.
# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/#performance-tuning
HEAP_SIZE_AT_BUILD_TIME="-R:MaximumHeapSizePercent=25"
# Performance tuning for Serial GC is limited. More performance tuning options
# are available for G1 GC, but G1 is not available in GraalVM Community Edition.

# DEBUG TIP: you can use these flags when RUNNING the binary (not when compiling it).
# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/#printing-garbage-collections

# CPU optimizations ############################################################

# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/#optimization-levels
# -Ob: quicker build time
# -O2: better performance
OPTIMIZATION_LEVEL="-O2"

# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/#optimizing-for-specific-machines
MACHINE_TYPE="-march=x86-64-v3"
# I tried using -march=native but it produced a binary that was not working on
# my machine. When I tried to execute it, I got the following error:
# The current machine does not support all of the following CPU features that are required by the image: [...]

# Use this command to list all the available machine types.
# native-image -march=list

# native-image does NOT support cross-compilation.
# https://github.com/oracle/graal/issues/407
ARCH=amd64
OS=linux
TARGET="--target=$OS-$ARCH"

APP_NAME=pod-jackdbd-jsoup
IMAGE_NAME=$APP_NAME
# IMAGE_NAME="$APP_NAME-$ARTIFACT_VERSION-$ARCH-$OS"
echo "IMAGE_NAME is $IMAGE_NAME"

## Native Image Builder ########################################################
# https://www.graalvm.org/22.0/reference-manual/native-image/Options/
# https://docs.oracle.com/en/graalvm/enterprise/22/docs/reference-manual/native-image/overview/BuildOptions/

# I am not sure I need to add this flag.
# -J-Dclojure.compiler.direct-linking=true
# https://clojure.org/reference/compilation#directlinking

# Why do we need to trigger class initialization at build time in this project?
# https://www.graalvm.org/latest/reference-manual/native-image/basics/#build-time-vs-run-time
# --initialize-at-build-time

# When running on NixOS, this works only when the environment variables CPATH,
# LIBRARY_PATH, NIX_LDFLAGS are set.
native-image -jar $UBERJAR_PATH $IMAGE_NAME \
  -H:ReflectionConfigurationFiles=reflection.json \
  -H:+ReportExceptionStackTraces \
  -J-Dclojure.compiler.direct-linking=true \
  $GARBAGE_COLLECTOR \
  $HEAP_SIZE_AT_BUILD_TIME \
  $OPTIMIZATION_LEVEL \
  $MACHINE_TYPE \
  $TARGET \
  --initialize-at-build-time \
  --native-image-info \
  --no-fallback \
  --static --libc=musl \
  --verbose

if [ "${CI+x}" ]; then
  # Avoid moving the artifact when running on GitHub actions (other steps in the
  # GitHub workflow expect the artifact to be here).
  echo "Binary artifact is at $IMAGE_NAME" 
else
  mv "$IMAGE_NAME" "target/$IMAGE_NAME"
  echo "Binary artifact moved to target/$IMAGE_NAME"
fi

# DEBUG TIP: you can use these flags when RUNNING the binary (not when compiling it).
# https://www.graalvm.org/latest/reference-manual/native-image/optimizations-and-performance/MemoryManagement/#printing-garbage-collections
