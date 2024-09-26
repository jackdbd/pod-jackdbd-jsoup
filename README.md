# pod-jackdbd-jsoup

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

```sh
./script/compile.sh
# or, in alternative:
bb build:native
```

Double check that the binary compiled with GraalVM native-image is statically linked.

```sh
ldd target/pod-jackdbd-jsoup-0.1.0
objdump --dynamic-syms target/pod-jackdbd-jsoup-0.1.0
```

Run a quick demo...

```sh
bb bb/how_to_use.bb
```

...or start a Babashka REPL and evaluate the forms in `how_to_use.bb`

## Tests

Run all tests

```sh
clj -M:test
```

## Troubleshooting

```sh
rm svm_err_*
rm -rf ~/.m2/repository/org/jsoup/
rm -rf ~/.cache/clojure-lsp/
```
