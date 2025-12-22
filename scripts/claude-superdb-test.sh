#!/usr/bin/env bash
# shellcheck shell=bash

# Convenience script for CLAUDE to test SuperDB queries interactively
# Usage: ./claude-superdb-test.sh [query|file.spq] [data|file]

set -euo pipefail

# Check for debug flag
if [[ "${1:-}" == "--debug" || "${1:-}" == "-x" ]]; then
    set -x
    shift
fi

# If no arguments provided, show usage
if [[ $# -eq 0 ]]; then
    cat <<EOF
Usage: $0 [--debug|-x] [query|file.spq] [data|file]

Run SuperDB queries with test data without needing approval.

Examples:
  # Extract field from JSON
  $0 'this.user_id' '{"user_id":"abc123","exp":123}'

  # Extract with values
  $0 'values user_id' '{"user_id":"abc123","exp":123}'

  # Complex query
  $0 'unnest this | where id > 5 | collect(this)' '[{"id":3},{"id":7}]'

  # Using a .spq file as query (include file)
  $0 examples/basic_read_data.spq examples/data.json
  $0 src/test/testData/parser/ConstDeclaration.spq

  # Using a data file (absolute or relative path)
  $0 'values this' src/e.json
  $0 'unnest events | count()' /path/to/file.json

  # Using stdin (data can be empty string)
  $0 'values this' '' < /path/to/file.sup

  # Debug mode
  $0 -x 'this.user_id' '{"user_id":"test"}'

Output modes (automatically added):
  -s  : Text output (default)
  -j  : JSON output (use when you need JSON)
  -f line : Line output (use for clean single values)

This script:
  - Runs in src directory (for access to .sup files)
  - Uses super with -s flag by default (text output)
  - Accepts .spq files as query include files (-I flag)
  - Accepts data via argument, file path, or stdin
  - Automatically detects file paths (checks if file exists)
  - Does NOT require approval for iterations
EOF
    exit 1
fi

include=""
query="$1"
data="${2:-}"

# If 3 args and first is a file: include_file query data
if [[ $# -ge 3 && -f "$1" ]]; then
    include="$1"
    query="$2"
    data="$3"
# If first arg is a file (2 args or less): query_file [data]
elif [[ -f "$query" ]]; then
    include="$query"
    query=""
fi

# Build command
cmd=(super -s)
[[ -n "$include" ]] && cmd+=(-I "$include")
[[ -n "$query" ]] && cmd+=(-c "$query")

# Run with appropriate data source
if [[ -z "$data" ]]; then
    "${cmd[@]}"
elif [[ -f "$data" ]]; then
    "${cmd[@]}" "$data"
else
    echo "$data" | "${cmd[@]}" -
fi