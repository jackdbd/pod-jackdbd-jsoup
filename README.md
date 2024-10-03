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

### Versioning

This project uses a `canary` branch for development and a `main` branch for production.

All versions created on the `canary` branch should be [SNAPSHOT](https://stackoverflow.com/a/5901460/3036129) versions.

When you introduce at least one **bug fix** on `canary`, run this command:

```sh
bb snapshot patch
```

When you introduce at least one **new feature** on `canary`, run this command:

```sh
bb snapshot minor
```

When you introduce at least one **breaking change** feature on `canary`, run this command:

```sh
bb snapshot major
```

All versions created on the `main` branch should be **stable** versions.

When a PR gets merged from `canary` to `main`, **immediately** run these commands:

```sh
git checkout main
git pull
bb stable # 1.2.3-SNAPSHOT->1.2.3 + commit + tag
git push
```

The command `bb stable` promotes the latest SNAPSHOT version to a stable version and creates an annotated git tag. Then `git push` triggers the CI/CD pipeline that creates a new GitHub release and publishes the uberjar to Clojars.

Switch back to `canary` and integrate the changes from `main`:

```sh
git checkout canary
git rebase origin/main
bb snapshot patch # version bump (patch|minor|major) + commit + tag
git push
```

### Registering the pod on the Pod registry

The [CI/CD pipeline](./.github/workflows/ci-cd.yaml) takes care of creating a GitHub release that includes the following assets:

- Binary executables for Linux, macOS and Windows.
- An uberjar (the one published to Clojars).
- A `manifest.edn` that can be used *register* the pod on the [Pod registry](https://github.com/babashka/pod-registry).

Download the `manifest.edn` and make a PR on the Pod registry following [these instructions](https://github.com/babashka/pod-registry?tab=readme-ov-file#registering-a-pod).

> [!IMPORTANT]
> Every pod version has its own `manifest.edn`.

Once the PR on [Pod registry](https://github.com/babashka/pod-registry) gets merged, the pod version will be considered *registered* and users will be able to load it using the **qualified keyword** for the pod and the desired **version**.
