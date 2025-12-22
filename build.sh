#!/usr/bin/env bash

set -euo pipefail

function _usage() {
  grep -E '^function [^_]' "${BASH_SOURCE[0]}" | sed 's/function /  /; s/().*//' | sort
}

function usage() {
  echo "Usage: ./build.sh <command>"
  echo ""
  echo "Commands:"
  echo "  build   Build plugin (clean + compile)"
  echo "  dev     Full dev cycle: download LSP + build + launch IDE"
  echo "  ide     Launch test IDE (quick, no rebuild)"
  echo "  lsp     Download latest LSP binaries from GitHub releases"
  echo "  sync    Instructions for syncing grammar with upstream"
  echo "  test    Run tests"
}

function _cd_root() {
  cd "$(dirname "${BASH_SOURCE[0]}")"
}

function lsp() {
  _cd_root
  echo "==> Downloading latest LSP binaries..."
  scripts/download-all-platforms.sh
}

function build() {
  _cd_root
  echo "==> Building plugin..."
  ./gradlew clean build
}

function ide() {
  _cd_root
  echo "==> Launching test IDE..."
  ./gradlew prepareSandbox runIde
}

function dev() {
  _cd_root
  echo "==> Full dev cycle: LSP + build + IDE..."
  lsp
  echo ""
  ./gradlew clean prepareSandbox runIde
}

function test() {
  _cd_root
  echo "==> Running tests..."
  ./gradlew test
}

function sync() {
  _cd_root
  echo "==> Run /sync in Claude Code to sync grammar with upstream"
  echo "    This updates keywords/operators from brimdata/super"
}

if [ $# -eq 0 ]; then
  usage
else
  "$@"
fi
