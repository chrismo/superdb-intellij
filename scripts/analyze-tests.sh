#!/bin/bash
# Analyze test results from Gradle test runs

set -e

RESULTS_DIR="${1:-build/test-results/test}"
REPORTS_DIR="${2:-build/reports/tests/test}"

echo "=== Test Results Summary ==="
echo ""

if [ ! -d "$RESULTS_DIR" ]; then
    echo "No test results found at $RESULTS_DIR"
    echo "Run './gradlew test' first"
    exit 1
fi

# Count totals
total_tests=0
total_failures=0
total_errors=0
total_skipped=0

for xml in "$RESULTS_DIR"/*.xml; do
    [ -f "$xml" ] || continue

    tests=$(grep -o 'tests="[0-9]*"' "$xml" | head -1 | grep -o '[0-9]*')
    failures=$(grep -o 'failures="[0-9]*"' "$xml" | head -1 | grep -o '[0-9]*')
    errors=$(grep -o 'errors="[0-9]*"' "$xml" | head -1 | grep -o '[0-9]*')
    skipped=$(grep -o 'skipped="[0-9]*"' "$xml" | head -1 | grep -o '[0-9]*')

    total_tests=$((total_tests + ${tests:-0}))
    total_failures=$((total_failures + ${failures:-0}))
    total_errors=$((total_errors + ${errors:-0}))
    total_skipped=$((total_skipped + ${skipped:-0}))
done

passed=$((total_tests - total_failures - total_errors - total_skipped))

echo "Total:    $total_tests"
echo "Passed:   $passed"
echo "Failed:   $total_failures"
echo "Errors:   $total_errors"
echo "Skipped:  $total_skipped"
echo ""

# Show failed tests
if [ $total_failures -gt 0 ] || [ $total_errors -gt 0 ]; then
    echo "=== Failed Tests ==="
    echo ""

    for xml in "$RESULTS_DIR"/*.xml; do
        [ -f "$xml" ] || continue

        classname=$(basename "$xml" .xml | sed 's/TEST-//')

        # Extract failure messages
        if grep -q '<failure' "$xml" || grep -q '<error' "$xml"; then
            echo "--- $classname ---"
            # Show test name and first line of failure message
            grep -B1 '<failure\|<error' "$xml" | grep 'testcase name' | sed 's/.*name="\([^"]*\)".*/  - \1/'
            echo ""
        fi
    done

    echo "=== Failure Details ==="
    echo ""

    for xml in "$RESULTS_DIR"/*.xml; do
        [ -f "$xml" ] || continue

        if grep -q '<failure' "$xml" || grep -q '<error' "$xml"; then
            classname=$(basename "$xml" .xml | sed 's/TEST-//')
            echo "--- $classname ---"
            # Extract failure type and message (first 5 lines)
            grep -A5 '<failure\|<error' "$xml" | head -20
            echo ""
        fi
    done
fi

echo "=== HTML Report ==="
echo "file://$PWD/$REPORTS_DIR/index.html"
