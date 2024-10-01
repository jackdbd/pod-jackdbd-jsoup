# pod-jackdbd-jsoup

![CI/CD](https://github.com/jackdbd/pod-jackdbd-jsoup/actions/workflows/ci-cd.yaml/badge.svg)
[![Clojars Project](https://img.shields.io/clojars/v/com.github.jackdbd/pod.jackdbd.jsoup.svg)](https://clojars.org/com.github.jackdbd/pod.jackdbd.jsoup)

Babashka pod for parsing HTML with [jsoup](https://jsoup.org/).

## Setup

The developer environment for this project is declared using [devenv](https://github.com/cachix/devenv).

This project is managed with [neil](https://github.com/babashka/neil) and [Babashka tasks](https://book.babashka.org/#tasks).

## Compile the pod

### JAR

```sh
clj -T:build jar
```

### Uber-JAR

```sh
clj -T:build uber
```

### Executable binary

If you are on Linux, you can compile a statically-linked binary with the following command, which uses GraalVM native-image with [musl](https://musl.libc.org/) support.

```sh
clj -T:build uber && ./script/compile.sh

# or, in alternative, just run the following command:
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

## Upgrade version

Use `neil` to update the version in `deps.edn`. Here are a few examples:

```sh
neil version set 0.1.0
neil version patch
neil version minor
```

These `neil` commands:

- Create a Git commit and tag (this can be bypassed with `--no-tag`)
- Require the working directory to be clean (this can be bypassed with `--force`)
