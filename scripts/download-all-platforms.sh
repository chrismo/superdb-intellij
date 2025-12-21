#!/bin/bash
# Download SuperSQL LSP binaries for all platforms (for distribution)
#
# Usage: ./scripts/download-all-platforms.sh [version]
#   version: Release version (default: latest)

set -euo pipefail

VERSION="${1:-latest}"
LSP_REPO="${LSP_REPO:-chrismo/superdb-syntaxes}"
OUTPUT_DIR="${OUTPUT_DIR:-src/main/resources/lsp}"

# Platform configurations
PLATFORMS=(
    "linux-amd64"
    "linux-arm64"
    "darwin-amd64"
    "darwin-arm64"
    "windows-amd64"
)

# Get release data once
get_release_data() {
    local version="$1"
    local api_url auth_header=""

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
        curl -sS -H "$auth_header" -H "Accept: application/vnd.github+json" "$api_url"
    else
        curl -sS -H "Accept: application/vnd.github+json" "$api_url"
    fi
}

# Extract asset URL from release data
get_asset_url() {
    local release_data="$1"
    local platform="$2"
    local binary_suffix=""

    if [[ "$platform" == windows-* ]]; then
        binary_suffix=".exe"
    fi

    # Try different naming patterns
    for pattern in "super-lsp-${platform}${binary_suffix}" \
                   "supersql-lsp-${platform}${binary_suffix}" \
                   "lsp-${platform}${binary_suffix}"; do
        local url
        url=$(echo "$release_data" | grep -o "\"browser_download_url\"[[:space:]]*:[[:space:]]*\"[^\"]*${pattern}[^\"]*\"" | head -1 | sed 's/.*"\(http[^"]*\)".*/\1/')
        if [[ -n "$url" ]]; then
            echo "$url"
            return 0
        fi
    done

    return 1
}

# Download a single binary
download_binary() {
    local url="$1"
    local output_file="$2"
    local auth_header=""

    if [[ -n "${GITHUB_TOKEN:-}" ]]; then
        auth_header="Authorization: token $GITHUB_TOKEN"
    fi

    mkdir -p "$(dirname "$output_file")"

    if [[ -n "$auth_header" ]]; then
        curl -sSL -H "$auth_header" -H "Accept: application/octet-stream" -o "$output_file" "$url"
    else
        curl -sSL -H "Accept: application/octet-stream" -o "$output_file" "$url"
    fi

    chmod +x "$output_file"
}

# Main
main() {
    local release_data
    release_data=$(get_release_data "$VERSION")

    # Extract version tag
    local tag_name
    tag_name=$(echo "$release_data" | grep -o '"tag_name"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*"\([^"]*\)"$/\1/')
    echo "Release version: $tag_name"

    mkdir -p "$OUTPUT_DIR"

    local downloaded=0
    local failed=0

    for platform in "${PLATFORMS[@]}"; do
        local url
        if url=$(get_asset_url "$release_data" "$platform"); then
            local filename="super-lsp-${platform}"
            if [[ "$platform" == windows-* ]]; then
                filename="${filename}.exe"
            fi

            echo "Downloading: $platform..."
            if download_binary "$url" "${OUTPUT_DIR}/${filename}"; then
                echo "  ✓ ${OUTPUT_DIR}/${filename}"
                ((downloaded++))
            else
                echo "  ✗ Failed to download $platform"
                ((failed++))
            fi
        else
            echo "  ⚠ No binary found for $platform (may not be supported)"
        fi
    done

    echo ""
    echo "Download complete: $downloaded succeeded, $failed failed"
    echo "Version: $tag_name"

    # Write version file
    echo "$tag_name" > "${OUTPUT_DIR}/VERSION"
    echo "Version written to: ${OUTPUT_DIR}/VERSION"
}

main "$@"
