# pod-jackdbd-jsoup

![CI/CD](https://github.com/jackdbd/pod-jackdbd-jsoup/actions/workflows/ci-cd.yaml/badge.svg)
[![Clojars Project](https://img.shields.io/clojars/v/com.github.jackdbd/pod.jackdbd.jsoup.svg)](https://clojars.org/com.github.jackdbd/pod.jackdbd.jsoup)

Babashka pod for parsing HTML with [jsoup](https://jsoup.org/).

## How to use it?

See [examples/jsoup.bb](./examples/jsoup.bb).

## Development

The developer environment for this project is declared using [devenv](https://github.com/cachix/devenv).

This project is managed with [neil](https://github.com/babashka/neil) and [Babashka tasks](https://book.babashka.org/#tasks). You can use `bb tasks` to view all available tasks.

### Linux binary

If you are on Linux, you can compile a statically-linked binary using `bb build:binary`.
Double check that the binary is statically linked.

```sh
ldd target/pod-jackdbd-jsoup
objdump --dynamic-syms target/pod-jackdbd
```

### Upgrade version

Use `neil` to update the version in `deps.edn`. Here are a few examples:

```sh
neil version set 0.1.0
neil version patch
neil version minor
```

A few things to keep in mind about `neil version`:

- it creates a Git commit and tag (this can be bypassed with `--no-tag`)
- it requires the working directory to be clean (this can be bypassed with `--force`)

At the moment it seems `neil version` creates a commit but not a tag. You will have to manually create an **annotated** tag. Here is an example:

```sh
git tag -a v0.1.2 -m "Version 0.1.2"
```

### Create manifest.edn

```sh
bb bb/manifest.bb \
  --license MIT \
  --pod-id "com.github.jackdbd/pod.jackdbd.jsoup" \
  --version 0.1.2 \
  --uberjar "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v0.1.2/pod-jackdbd-jsoup-0.1.2-ubuntu-latest-x86_64.zip" \
  --linux-x86_64 "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v0.1.2/pod-jackdbd-jsoup-0.1.2-ubuntu-latest-x86_64.zip" \
  --macos-aarch64 "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v0.1.2/pod-jackdbd-jsoup-0.1.2-macos-latest-aarch64.zip" \
  --windows-x86_64 "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v0.1.2/pod-jackdbd-jsoup-0.1.2-windows-latest-x86_64.zip"
```
