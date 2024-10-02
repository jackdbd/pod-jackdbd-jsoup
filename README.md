# pod-jackdbd-jsoup

![CI/CD](https://github.com/jackdbd/pod-jackdbd-jsoup/actions/workflows/ci-cd.yaml/badge.svg)
[![Clojars Project](https://img.shields.io/clojars/v/com.github.jackdbd/pod.jackdbd.jsoup.svg)](https://clojars.org/com.github.jackdbd/pod.jackdbd.jsoup)

Babashka pod for parsing HTML with [jsoup](https://jsoup.org/).

## How to use it?

If you want to load a version of this pod that was *registered* (see below) on the [Pod registry](https://github.com/babashka/pod-registry), you can do it using the **qualified keyword** of this pod.

```clj
(require '[babashka.pods :as pods])
(pods/load-pod 'com.github.jackdbd/pod.jackdbd.jsoup "0.1.2")
```

Whether the pod is *registered* or not, you can always load it with this method:

1. Download the archive (e.g. `.zip`) containing the pod binary for your OS/architecture from a [GitHub release](https://github.com/jackdbd/pod-jackdbd-jsoup/releases).
1. Extract the binary from the archive (e.g. `unzip` it).
1. Move the binary to your [`BABASHKA_PODS_DIR`](https://github.com/babashka/pods?tab=readme-ov-file#where-does-the-pod-come-from).
1. Load the pod using its **file name**.

```clj
(require '[babashka.pods :as pods])
(pods/load-pod "pod-jackdbd-jsoup")
```


See also [examples/jsoup.bb](./examples/jsoup.bb).

## Development

The file [`devenv.nix`](./devenv.nix) declares a developer environment for this project. This file is used by [devenv](https://github.com/cachix/devenv) to create such environment. If you don't use devenv you can ignore this file, or use it to understand which dependencies are required by this project.

This project uses a [`bb.edn`](./bb.edn) file to define a few [Babashka tasks](https://book.babashka.org/#tasks). You can type `bb tasks` to view them.

### Linux binary

If you are on Linux, you can compile a statically-linked binary using this Babashka task.

```sh
bb build:binary
```

You can double check that the binary is statically linked using one of the following commands.

```sh
ldd target/pod-jackdbd-jsoup
objdump --dynamic-syms target/pod-jackdbd
```

### Upgrade version

At the moment `neil version` creates a git commit but fails to create a git tag. You can use the following commands instead.

```sh
bb bump:patch
bb tag
```

These commands bump the patch version and create an annotated git tag.

### Create `manifest.edn` and register the pod on Pod registry

The [CI/CD pipeline](./.github/workflows/ci-cd.yaml) takes care of creating a GitHub release with compilation artifacts for Linux, macOS and Windows.

After all compilation artifacts for one pod version have been uploaded to the GitHub release associated with that version, that pod version can be *registered* in the [Pod registry](https://github.com/babashka/pod-registry).

> [!IMPORTANT]
> Every pod version has its own `manifest.edn`.

Currently, registering a pod involves two manual steps.

First, create a `manifest.edn` file for one version of the pod.

```sh
VERSION=0.1.4 && \
bb bb/manifest.bb \
  --version $VERSION \
  --uberjar "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v$VERSION/pod-jackdbd-jsoup-$VERSION-ubuntu-latest-x86_64.zip" \
  --linux-x86_64 "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v$VERSION/pod-jackdbd-jsoup-$VERSION-ubuntu-latest-x86_64.zip" \
  --macos-aarch64 "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v$VERSION/pod-jackdbd-jsoup-$VERSION-macos-latest-aarch64.zip" \
  --windows-x86_64 "https://github.com/jackdbd/pod-jackdbd-jsoup/releases/download/v$VERSION/pod-jackdbd-jsoup-$VERSION-windows-latest-x86_64.zip"
```

Then, make a PR on Pod registry following [these instructions](https://github.com/babashka/pod-registry?tab=readme-ov-file#registering-a-pod).

Once the PR on [Pod registry](https://github.com/babashka/pod-registry) gets merged, that pod version will be considered *registered* and users will be able to load it using the **qualified keyword** for the pod and the desired **version**.
