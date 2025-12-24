#!/bin/bash
# Download SuperSQL LSP binaries for all platforms (for distribution)
#
# Usage: ./scripts/download-all-platforms.sh [version]
#   version: Release version tag (default: latest)
#
# Supports: gh (GitHub CLI) preferred, falls back to curl + super

set -euo pipefail

VERSION="${1:-}"
LSP_REPO="${LSP_REPO:-chrismo/superdb-syntaxes}"
OUTPUT_DIR="${OUTPUT_DIR:-src/main/resources/lsp}"

PLATFORMS=(
    "linux-amd64"
    "linux-arm64"
    "darwin-amd64"
    "darwin-arm64"
    "windows-amd64"
)

mkdir -p "$OUTPUT_DIR"

# Try gh CLI first (simpler)
if command -v gh &> /dev/null; then
    echo "Downloading LSP binaries from $LSP_REPO using gh..."

    # Resolve "latest" to actual tag (gh release download needs explicit tag in some environments)
    if [[ -z "$VERSION" || "$VERSION" == "latest" ]]; then
        VERSION=$(gh release view --repo "$LSP_REPO" --json tagName -q '.tagName')
        echo "  Resolved latest to: $VERSION"
    fi

    gh release download "$VERSION" --repo "$LSP_REPO" --pattern 'superdb-lsp-*' --dir "$OUTPUT_DIR" --clobber

    DOWNLOADED_VERSION="$VERSION"

# Fallback to curl + super for JSON parsing
elif command -v super &> /dev/null; then
    echo "Downloading LSP binaries from $LSP_REPO using curl + super..."

    API_URL="https://api.github.com/repos/${LSP_REPO}/releases/latest"
    if [[ -n "$VERSION" && "$VERSION" != "latest" ]]; then
        API_URL="https://api.github.com/repos/${LSP_REPO}/releases/tags/${VERSION}"
    fi

    RELEASE_JSON=$(curl -sS -H "Accept: application/vnd.github+json" "$API_URL")
    DOWNLOADED_VERSION=$(echo "$RELEASE_JSON" | super -f text -c 'tag_name')

    for platform in "${PLATFORMS[@]}"; do
        binary_name="superdb-lsp-${platform}"
        if [[ "$platform" == windows-* ]]; then
            binary_name="${binary_name}.exe"
        fi

        url=$(echo "$RELEASE_JSON" | super -f text -c "assets | where name == \"$binary_name\" | browser_download_url" 2>/dev/null || true)

        if [[ -n "$url" ]]; then
            echo "  Downloading $binary_name..."
            curl -sSL -o "${OUTPUT_DIR}/${binary_name}" "$url"
        else
            echo "  ⚠ No binary found for $platform"
        fi
    done
else
    echo "Error: Neither 'gh' nor 'super' command found." >&2
    echo "Install GitHub CLI (gh) or SuperDB CLI (super) to download LSP binaries." >&2
    exit 1
fi

# Make binaries executable
chmod +x "$OUTPUT_DIR"/superdb-lsp-* 2>/dev/null || true

# Write version file
echo "$DOWNLOADED_VERSION" > "$OUTPUT_DIR/VERSION"

echo ""
echo "Downloaded LSP binaries:"
ls -la "$OUTPUT_DIR"/superdb-lsp-* 2>/dev/null || echo "  (none found)"
echo ""
echo "Version: $DOWNLOADED_VERSION"
echo "Version written to: $OUTPUT_DIR/VERSION"
