#!/usr/bin/env bash
set -euo pipefail

# https://github.com/jackdbd/pod-registry/tree/master?tab=readme-ov-file#registering-a-pod

POD_REGISTRY="$HOME/repos/pod-registry"
ORG=com.github.jackdbd # Group ID
POD_NAME=jsoup
MANIFEST_DIR="$POD_REGISTRY/manifests/$ORG/$POD_NAME/$POD_VERSION"
EXAMPLES_DIR="$POD_REGISTRY/examples"

echo "Download manifest.edn from GitHub release v$POD_VERSION"
gh release download "v$POD_VERSION" --clobber --pattern manifest.edn

echo "Register pod $POD_NAME at $MANIFEST_DIR"
mkdir -p $MANIFEST_DIR
mv manifest.edn $MANIFEST_DIR

echo "Copy examples/$POD_NAME.bb to $EXAMPLES_DIR"
cp "examples/$POD_NAME.bb" $EXAMPLES_DIR

echo "TODO: commit to https://github.com/jackdbd/pod-registry/ and open a PR on https://github.com/babashka/pod-registry"
