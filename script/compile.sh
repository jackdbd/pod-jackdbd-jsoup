#!/usr/bin/env bash
set -euo pipefail

POD_ID=pod.jackdbd.jsoup
POD_NAME=pod-jackdbd-jsoup
POD_VERSION=$(bb -e '(-> (slurp "deps.edn") edn/read-string :aliases :neil :project :version)' | tr -d '"')
UBERJAR_PATH="target/$POD_ID-$POD_VERSION-standalone.jar"
# echo "UBERJAR_PATH is $UBERJAR_PATH"

# Entry point of the GraalVM native-image documentation.
# https://www.graalvm.org/latest/reference-manual/native-image/
# https://www.graalvm.org/latest/reference-manual/native-image/overview/BuildOutput/

HEAP_SIZE_AT_BUILD_TIME="-R:MaxHeapSize=1024m"

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
TARGET="linux-amd64"

# https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#default-environment-variables
if [ "${CI+x}" ]; then
  echo "Running on GitHub actions"
else
  echo "NOT running on GitHub actions"
fi

# I am not I need to add this flag.
# -J-Dclojure.compiler.direct-linking=true
# https://clojure.org/reference/compilation#directlinking

# When running on NixOS, this works only when the environment variables CPATH,
# LIBRARY_PATH, NIX_LDFLAGS are set.
native-image -jar $UBERJAR_PATH \
  -H:ReflectionConfigurationFiles=reflection.json \
  -H:+ReportExceptionStackTraces \
  -J-Dclojure.compiler.direct-linking=true \
  $HEAP_SIZE_AT_BUILD_TIME \
  $OPTIMIZATION_LEVEL \
  $MACHINE_TYPE \
  --initialize-at-build-time \
  --native-image-info \
  --no-fallback \
  --report-unsupported-elements-at-runtime \
  --static --libc=musl \
  "--target=$TARGET" \
  --verbose

mv "$POD_ID-$POD_VERSION-standalone" "target/$POD_NAME"
echo "Binary artifact moved to target/$POD_NAME"
