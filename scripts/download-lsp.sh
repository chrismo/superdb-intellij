#!/bin/bash
# Download SuperSQL LSP binary from chrismo/superdb-syntaxes releases
#
# Usage: ./scripts/download-lsp.sh [version]
#   version: Release version (default: latest)
#
# Environment variables:
#   GITHUB_TOKEN: Optional, for accessing private repos or higher rate limits
#   LSP_REPO: Repository to fetch from (default: chrismo/superdb-syntaxes)

set -euo pipefail

VERSION="${1:-latest}"
LSP_REPO="${LSP_REPO:-chrismo/superdb-syntaxes}"
OUTPUT_DIR="${OUTPUT_DIR:-src/main/resources/lsp}"

# Determine platform and architecture
detect_platform() {
    local os arch

    case "$(uname -s)" in
        Linux*)  os="linux" ;;
        Darwin*) os="darwin" ;;
        MINGW*|MSYS*|CYGWIN*) os="windows" ;;
        *)       echo "Unsupported OS: $(uname -s)" >&2; exit 1 ;;
    esac

    case "$(uname -m)" in
        x86_64|amd64) arch="amd64" ;;
        arm64|aarch64) arch="arm64" ;;
        *)            echo "Unsupported architecture: $(uname -m)" >&2; exit 1 ;;
    esac

    echo "${os}-${arch}"
}

# Get the download URL for a specific release
get_release_url() {
    local version="$1"
    local platform="$2"
    local api_url release_data asset_url

    # Build auth header if token is available
    local auth_header=""
    if [[ -n "${GITHUB_TOKEN:-}" ]]; then
        auth_header="Authorization: token $GITHUB_TOKEN"
    fi

    if [[ "$version" == "latest" ]]; then
        api_url="https://api.github.com/repos/${LSP_REPO}/releases/latest"
    else
        api_url="https://api.github.com/repos/${LSP_REPO}/releases/tags/${version}"
    fi

    echo "Fetching release info from: $api_url" >&2

    if [[ -n "$auth_header" ]]; then
        release_data=$(curl -sS -H "$auth_header" -H "Accept: application/vnd.github+json" "$api_url")
    else
        release_data=$(curl -sS -H "Accept: application/vnd.github+json" "$api_url")
    fi

    # Check for errors
    if echo "$release_data" | grep -q '"message".*"Not Found"'; then
        echo "Error: Release not found" >&2
        exit 1
    fi

    # LSP binary naming convention: superdb-lsp-{os}-{arch}
    local binary_suffix=""
    if [[ "$platform" == windows-* ]]; then
        binary_suffix=".exe"
    fi

    # Try to find matching asset (primary: superdb-lsp-*, fallback: super-lsp-*)
    for pattern in "superdb-lsp-${platform}${binary_suffix}" \
                   "super-lsp-${platform}${binary_suffix}" \
                   "supersql-lsp-${platform}${binary_suffix}"; do
        asset_url=$(echo "$release_data" | grep -o "\"browser_download_url\"[[:space:]]*:[[:space:]]*\"[^\"]*${pattern}[^\"]*\"" | head -1 | sed 's/.*"\(http[^"]*\)".*/\1/')
        if [[ -n "$asset_url" ]]; then
            echo "$asset_url"
            return 0
        fi
    done

    # If no exact match, list available assets for debugging
    echo "Available assets:" >&2
    echo "$release_data" | grep -o '"name"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/"name"[[:space:]]*:[[:space:]]*"\([^"]*\)"/  - \1/' >&2

    echo "Error: No matching LSP binary found for platform: $platform" >&2
    exit 1
}

# Download the LSP binary
download_lsp() {
    local url="$1"
    local output_file="$2"
    local auth_header=""

    if [[ -n "${GITHUB_TOKEN:-}" ]]; then
        auth_header="Authorization: token $GITHUB_TOKEN"
    fi

    echo "Downloading LSP from: $url"

    mkdir -p "$(dirname "$output_file")"

    if [[ -n "$auth_header" ]]; then
        curl -sSL -H "$auth_header" -H "Accept: application/octet-stream" -o "$output_file" "$url"
    else
        curl -sSL -H "Accept: application/octet-stream" -o "$output_file" "$url"
    fi

    chmod +x "$output_file"
    echo "Downloaded to: $output_file"
}

# Main
main() {
    local platform
    platform=$(detect_platform)
    echo "Detected platform: $platform"

    local url
    url=$(get_release_url "$VERSION" "$platform")

    local binary_name="super-lsp"
    if [[ "$platform" == windows-* ]]; then
        binary_name="super-lsp.exe"
    fi

    download_lsp "$url" "${OUTPUT_DIR}/${binary_name}"

    echo ""
    echo "LSP binary ready at: ${OUTPUT_DIR}/${binary_name}"
    echo "Version: $VERSION"
}

main "$@"
