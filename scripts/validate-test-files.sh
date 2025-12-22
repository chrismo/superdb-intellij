#!/bin/bash
# Validate test .spq files against the real SuperDB parser

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJ_DIR="$(dirname "$SCRIPT_DIR")"
TEST_DIR="$PROJ_DIR/src/test/testData/parser"

# Files that are intentionally broken (error recovery tests)
EXPECTED_BROKEN="IncompleteSelect.spq MissingParenthesis.spq"

echo "=== Validating test .spq files against SuperDB parser ==="
echo ""

passed=0
expected_fail=0
missing_data=0
parse_errors=0
parse_error_files=()

for spq in "$TEST_DIR"/*.spq; do
    [ -f "$spq" ] || continue
    name=$(basename "$spq")

    # Check if this is an expected broken file
    if echo "$EXPECTED_BROKEN" | grep -q "$name"; then
        echo "SKIP: $name (intentionally broken)"
        ((expected_fail++))
        continue
    fi

    # Run through super parser
    output=$("$SCRIPT_DIR/claude-superdb-test.sh" "$spq" 2>&1) || true

    if echo "$output" | grep -q "file does not exist"; then
        # Missing data file - query is syntactically valid but needs data
        echo "PASS: $name (needs data)"
        ((missing_data++))
    elif echo "$output" | grep -q "parse error\|ambiguous\|not.*supported"; then
        echo "FAIL: $name"
        ((parse_errors++))
        parse_error_files+=("$name")
    else
        echo "PASS: $name"
        ((passed++))
    fi
done

echo ""
echo "=== Summary ==="
echo "Passed:           $passed"
echo "Needs data:       $missing_data (valid syntax, missing data source)"
echo "Expected broken:  $expected_fail (error recovery tests)"
echo "Parse errors:     $parse_errors"

if [ $parse_errors -gt 0 ]; then
    echo ""
    echo "=== Parse Errors (need fixing) ==="
    for f in "${parse_error_files[@]}"; do
        echo ""
        echo "--- $f ---"
        "$SCRIPT_DIR/claude-superdb-test.sh" "$TEST_DIR/$f" 2>&1 | head -5
    done
    exit 1
fi
