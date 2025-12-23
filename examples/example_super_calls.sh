#!/bin/bash
# Example shell script with various SuperDB inline queries
# Used for testing language injection

# Basic query with double quotes
super -c "from data.json | where status == 'active'"

# Query with single quotes
super -c 'from data.json | head 10'

# Multi-line query (using double quotes)
# language=SuperDB
super -c "from events.json
| where timestamp > now() - 1h
| sort -r timestamp
| head 100"

# Using --command instead of -c
super --command "select * from file('data.parquet') | count()"

# Query with output redirection
super -c "
  from input.json
  | values {id, name, count: len(items)}
" > output.zson

# Query with input piped in - and old yield syntax that should be marked incorrect
cat data.json | super -c "yield {x: this}"

# Query in a variable assignment
RESULT=$(super -c "from data.json | count()")

# Query in an if statement
if super -c "from data.json | exists(status == 'error')" > /dev/null 2>&1; then
    echo "Found errors"
fi

# Query in a for loop context
for file in *.json; do
    super -c "from '$file' | head 1"
done

# Complex query with aggregation
super -c "from logs.json
| where level in ('error', 'warn')
| group by hour:=bucket(timestamp, 1h)
| yield {hour, error_count: count()}"

# Query with type casting
super -c "from data.json | yield {ts: cast(timestamp, <time>)}"

# Query with function call
super -c "from data.json | yield {upper_name: upper(name)}"

# NOT a SuperDB query - these should NOT be injected:
echo "from data.json | this is just a string"
grep -c "pattern" file.txt
curl -c cookies.txt https://example.com
