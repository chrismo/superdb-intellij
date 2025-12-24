#!/usr/bin/env bash

set -euo pipefail

function _usage() {
  usage | less -FX
}

function usage() {
  echo "Usage: ./build.sh <command>"
  echo ""
  echo "Leaf commands:"
  echo "  compile            Compile plugin"
  echo "  ide                Launch test IDE sandbox"
  echo "  lsp                Download latest LSP binaries"
  echo "  version            Show current version from git tags"
  echo "  sync               Instructions for syncing grammar"
  echo "  update-ide-versions  Auto-update verifier IDE versions"
  echo "  show-ide-versions    Show configured IDE versions"
  echo ""
  echo "Composite commands:"
  echo "  test               compile + run tests"
  echo "  package            compile + zip (no tests)"
  echo "  install-local      package + install to real IDE + restart"
  echo "  release <ver>      test + package + git tag"
  echo ""
  echo "Dependency graph:"
  echo ""
  echo "  test ─────────────────> compile"
  echo "  package ──────────────> compile"
  echo ""
  echo "  install-local ───────┬─> package ──> compile"
  echo "                       └─> (stop, install, start IDE)"
  echo ""
  echo "  release ─────────────┬─> test ────> compile"
  echo "                       ├─> package ─> compile"
  echo "                       └─> (git tag)"
}

function _cd_root() {
  cd "$(dirname "${BASH_SOURCE[0]}")"
}

function _clean_generated() {
  rm -rf src/main/gen build
}

function lsp() {
  _cd_root
  echo "==> Downloading latest LSP binaries..."
  scripts/download-all-platforms.sh
}

function compile() {
  _cd_root
  echo "==> Compiling plugin..."
  _clean_generated
  ./gradlew classes
}

function test() {
  _cd_root
  echo "==> Running tests..."
  compile
  ./gradlew test
}

function package() {
  _cd_root
  compile
  echo "==> Packaging plugin..."
  ./gradlew buildPlugin
  echo ""
  echo "Plugin zip created at:"
  ls -la "$(pwd)/build/distributions/"*.zip
}

function _copy_examples() {
  local dest="build/test-project"
  mkdir -p "$dest"
  cp -r examples/* "$dest/"
  echo "    Copied examples to $dest"
}

function ide() {
  _cd_root
  echo "==> Launching test IDE..."
  _copy_examples
  ./gradlew prepareSandbox runIde --args="$(pwd)/build/test-project"
}

function _wait_for_process_exit() {
  local process_name="$1"
  local timeout="${2:-30}"
  local elapsed=0

  while pgrep -x "$process_name" > /dev/null 2>&1; do
    if [ "$elapsed" -ge "$timeout" ]; then
      echo "    Timeout waiting for $process_name to exit"
      return 1
    fi
    sleep 1
    elapsed=$((elapsed + 1))
    printf "."
  done
  echo ""
  return 0
}

function install-local() {
  _cd_root
  local ide_name="${1:-RustRover}"
  local process_name
  process_name=$(echo "$ide_name" | tr '[:upper:]' '[:lower:]')

  # Build package
  package

  # Find the plugin zip
  local zip_file
  zip_file=$(ls -t build/distributions/*.zip 2>/dev/null | head -1)
  if [ -z "$zip_file" ]; then
    echo "Error: No plugin zip found in build/distributions/"
    exit 1
  fi

  # Find the IDE plugins directory
  local plugins_dir
  plugins_dir=$(ls -dt ~/Library/Application\ Support/JetBrains/${ide_name}* 2>/dev/null | head -1)
  if [ -z "$plugins_dir" ]; then
    echo "Error: Could not find $ide_name plugins directory"
    echo "Available IDEs:"
    ls ~/Library/Application\ Support/JetBrains/ 2>/dev/null
    exit 1
  fi
  plugins_dir="$plugins_dir/plugins"

  # Stop IDE if running
  if pgrep -x "$process_name" > /dev/null 2>&1; then
    echo "==> Stopping $ide_name..."
    pkill -x "$process_name"
    printf "    Waiting for exit"
    if ! _wait_for_process_exit "$process_name" 30; then
      echo "Error: IDE didn't stop. Try: pkill -9 $process_name"
      exit 1
    fi
  fi

  echo "==> Installing to: $plugins_dir"

  # Remove old installation
  rm -rf "$plugins_dir/superdb-intellij"

  # Unzip new version
  unzip -q "$zip_file" -d "$plugins_dir"

  echo "==> Installed $(basename "$zip_file")"

  # Restart IDE
  echo "==> Starting $ide_name..."
  open -a "$ide_name"
}

function sync() {
  _cd_root
  echo "==> Run /sync in Claude Code to sync grammar with upstream"
  echo "    This updates keywords/operators from brimdata/super"
}

function version() {
  _cd_root
  local ver
  ver=$(./gradlew -q properties | grep '^version:' | awk '{print $2}')
  echo "$ver"
}

function add-ide() {
  _cd_root
  local ver="${1:-}"

  if [ -z "$ver" ]; then
    echo "Error: IDE version required"
    echo "Usage: ./build.sh add-ide <version>"
    echo "Example: ./build.sh add-ide 2026.1"
    exit 1
  fi

  # Validate version format (YYYY.N)
  if ! [[ "$ver" =~ ^20[0-9]{2}\.[0-9]+$ ]]; then
    echo "Error: Invalid IDE version format '$ver'"
    echo "Expected: YYYY.N (e.g., 2026.1)"
    exit 1
  fi

  # Extract short version for gradle (e.g., 2026.1 -> 2026.1)
  local short_ver="$ver"

  echo "==> Adding IDE version $ver..."
  echo ""

  # 1. Update build.gradle.kts
  local gradle_file="build.gradle.kts"
  if grep -q "\"$short_ver\"" "$gradle_file"; then
    echo "    $gradle_file: Already has $short_ver"
  else
    # Find the last IDE version in runPluginVerifier and add after it
    sed -i.bak -E "s/(ideVersions\.set\(listOf\([^)]*)(\"20[0-9]{2}\.[0-9]+\")\)/\1\2, \"$short_ver\")/" "$gradle_file"
    rm -f "$gradle_file.bak"
    echo "    $gradle_file: Added $short_ver to runPluginVerifier"
  fi

  # 2. Update CI workflow
  local ci_file=".github/workflows/ci.yml"
  if grep -q "'$ver'" "$ci_file"; then
    echo "    $ci_file: Already has $ver"
  else
    # Add new version after the last one in the matrix
    sed -i.bak "/ide-version:/,/steps:/ {
      /- '20[0-9]\{2\}\.[0-9]\+'$/ {
        N
        s/\(- '20[0-9]\{2\}\.[0-9]\+'\)\n/\1\n          - '$ver'\n/
      }
    }" "$ci_file"
    rm -f "$ci_file.bak"
    echo "    $ci_file: Added $ver to compatibility matrix"
  fi

  echo ""
  echo "==> Done! Files updated:"
  echo "    - build.gradle.kts (runPluginVerifier)"
  echo "    - .github/workflows/ci.yml (compatibility matrix)"
  echo ""
  echo "Review changes with: git diff"
  echo "Then run: ./gradlew runPluginVerifier"
}

function show-ide-versions() {
  _cd_root
  echo "==> IDE versions configured for testing:"
  echo ""
  echo "build.gradle.kts (runPluginVerifier):"
  grep -oE '"20[0-9]{2}\.[0-9]+\.[0-9]+"' build.gradle.kts | tr -d '"' | sed 's/^/    /'
  echo ""
  echo ".github/workflows/ci.yml (compatibility matrix):"
  grep -E "^\s+- '20[0-9]{2}\.[0-9]+'" .github/workflows/ci.yml | sed "s/.*'\(.*\)'.*/    \1/"
}

function update-ide-versions() {
  _cd_root
  echo "==> Updating IDE versions for plugin verifier..."

  # Fetch fresh release data
  ./gradlew downloadIdeaProductReleasesXml --quiet 2>/dev/null

  local xml_file="build/tmp/downloadIdeaProductReleasesXml/idea_product_releases.xml"
  if [ ! -f "$xml_file" ]; then
    echo "Error: Could not fetch IDE releases XML"
    exit 1
  fi

  # Extract all IC versions from 2024 onwards (matching sinceBuild=241)
  local versions
  versions=$(grep -A500 '<code>IC</code>' "$xml_file" | grep -oE 'version="202[4-9][^"]*"' | sed 's/version="//;s/"//' | sort -V)

  if [ -z "$versions" ]; then
    echo "Error: No IC versions found in releases XML"
    exit 1
  fi

  # Get oldest supported major version's latest point release (2024.1.x)
  local oldest
  oldest=$(echo "$versions" | grep '^2024\.1\.' | tail -1)

  # Get newest version's latest point release
  local newest
  newest=$(echo "$versions" | tail -1)

  if [ -z "$oldest" ] || [ -z "$newest" ]; then
    echo "Error: Could not determine oldest/newest versions"
    exit 1
  fi

  echo "    Oldest supported: $oldest"
  echo "    Newest available: $newest"

  # Update build.gradle.kts
  local gradle_file="build.gradle.kts"
  sed -i.bak -E "s/ideVersions\.set\(listOf\([^)]+\)\)/ideVersions.set(listOf(\"$oldest\", \"$newest\"))/" "$gradle_file"
  rm -f "$gradle_file.bak"

  echo ""
  echo "==> Updated $gradle_file"
  echo ""
  grep -A1 'ideVersions.set' "$gradle_file" | head -2
}

function release() {
  _cd_root
  local ver="${1:-}"

  if [ -z "$ver" ]; then
    echo "Error: Version required"
    echo "Usage: ./build.sh release <version>"
    echo "Example: ./build.sh release 0.51222.0"
    exit 1
  fi

  # Validate version format (pre-release: X.XXXXX.X or post-release: X.X.X.X)
  if ! [[ "$ver" =~ ^[0-9]+\.[0-9]+\.[0-9]+(\.[0-9]+)?$ ]]; then
    echo "Error: Invalid version format '$ver'"
    echo "Expected: X.XXXXX.X (e.g., 0.51222.0) or X.X.X.X (e.g., 1.0.0.0)"
    exit 1
  fi

  local tag="v$ver"

  # Check if tag already exists
  if git rev-parse "$tag" >/dev/null 2>&1; then
    echo "Error: Tag '$tag' already exists"
    exit 1
  fi

  # Check for uncommitted changes
  if ! git diff-index --quiet HEAD --; then
    echo "Error: Uncommitted changes detected. Commit or stash them first."
    exit 1
  fi

  echo "==> Creating release $ver..."
  echo ""

  # Run tests first
  test

  # Create annotated tag
  git tag -a "$tag" -m "Release $ver"
  echo "    Created tag: $tag"

  # Build package
  package

  echo ""
  echo "==> Release $ver ready!"
  echo ""
  echo "Next steps:"
  echo "  git push --tags              # Push tag to remote"
  echo "  # Then create GitHub release with the zip file"
}

if [ $# -eq 0 ]; then
  _usage
else
  "$@"
fi
