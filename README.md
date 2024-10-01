# pod-jackdbd-jsoup

![CI/CD](https://github.com/jackdbd/pod-jackdbd-jsoup/actions/workflows/ci-cd.yaml/badge.svg)

<!-- [![Clojars Project](https://img.shields.io/clojars/v/degree9/featherscript.svg)](https://clojars.org/com.github.jackdbd/pod.jackdbd.jsoup) -->

<!-- [![Dependencies Status](https://versions.deps.co/degree9/featherscript/status.svg)](https://versions.deps.co/degree9/featherscript) -->

<!-- [![Downloads](https://versions.deps.co/degree9/featherscript/downloads.svg)](https://versions.deps.co/degree9/featherscript) -->

Babashka pod for HTML parsing with [jsoup](https://jsoup.org/).

## Setup

The developer environment for this project is declared using [devenv](https://github.com/cachix/devenv).

This project is managed with [neil](https://github.com/babashka/neil) and [Babashka tasks](https://book.babashka.org/#tasks).

## Package the pod into an uberjar

```sh
clj -T:build uber
# or, in alternative:
bb build:uberjar
```

## Compile the pod into an executable binary

If you are on Linux, you can compile a statically-linked binary with the following command, which uses GraalVM native-image with [musl](https://musl.libc.org/) support.

```sh
./script/compile.sh
# or, in alternative:
bb build:native
```

Double check that the binary is statically linked.

```sh
ldd target/pod-jackdbd-jsoup
objdump --dynamic-syms target/pod-jackdbd
```

Run a quick demo...

```sh
bb bb/how_to_use.bb
```

...or start a Babashka REPL and evaluate the forms in `how_to_use.bb`

## Tests

Run all tests

```sh
clj -X:test
```
