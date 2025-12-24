#!/bin/bash
# Example shell script with various SuperDB inline queries
# Used for testing language injection
#
# INJECTION SUPPORT SUMMARY:
# =========================
# BashSupport Pro (paid plugin):
#   - Automatic injection for: super -c "...", super --command "..."
#   - Automatic injection for heredocs with quoted markers: super -c <<"EOF" ... EOF
#   - Marker-based injection: <<SUPERSQL, <<SUPER, <<SPQ, <<ZQ, <<SUPERDB
#
# Built-in Shell Plugin (free):
#   - Comment-based injection only: # language=SuperDB

# ==============================================================================
# SECTION 1: String-based queries (BashSupport Pro - automatic injection)
# ==============================================================================

# Basic query with double quotes
super -c "from data.json | where status == 'active'"

# Query with options before -c
super -s -c "from data.json | where is(<record>)"

# Query with multiple options before -c
super -f line -s -c "from data.json | count()"

# Query with single quotes
super -c 'from data.json | head 10'

# Multi-line query in double quotes
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

# Query with input piped in
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

# ==============================================================================
# SECTION 2: Heredoc queries with quoted markers (BashSupport Pro - automatic)
# These use quoted markers like <<"EOF" which DISABLE variable expansion,
# making them valid injection hosts.
# ==============================================================================

# Heredoc with quoted marker (best practice for injection)
super -c <<"EOF"
from events.json
| where level == 'error'
| sort -r timestamp
| head 50
EOF

# Heredoc with single-quoted marker
super -c <<'QUERY'
from metrics.json
| where value > 100
| group by name
| yield {name, avg: avg(value), max: max(value)}
QUERY

# Heredoc using --command flag
super --command <<"END"
from users.json
| where active == true
| join (from orders.json) on user_id
| yield {name, order_count: count()}
END

# Heredoc with output redirection
super -c <<"SQL" > results.json
select * from file('data.parquet')
where timestamp > '2024-01-01'
order by timestamp desc
limit 1000
SQL

# ==============================================================================
# SECTION 3: Marker-based heredoc injection (BashSupport Pro - automatic)
# These heredocs use recognized SuperDB markers and get injection
# regardless of the command name.
# ==============================================================================

# Using SUPERSQL marker - inject even without super command
cat <<"SUPERSQL" | super
from data.json
| where status == 'active'
| count()
SUPERSQL

# Using SUPER marker
mysql_result | super -f json | cat <<"SUPER"
from stdin()
| transform {id, name: upper(name)}
SUPER

# Using SPQ marker (SuperDB Query)
cat <<"SPQ" > query.spq
-- This is a reusable query
from events.json
| where timestamp > now() - 24h
| group by hour:=bucket(timestamp, 1h)
| yield {hour, count: count()}
SPQ

# Using ZQ marker (zq compatibility)
cat data.json | zq -i json <<"ZQ"
from stdin()
| where severity in ('high', 'critical')
| sort -r severity
ZQ

# Using SUPERDB marker
super <<"SUPERDB"
from logs/*.json
| where level == 'error'
| fork (
  => count() | yield {total_errors: this}
  => group by service | yield {service, errors: count()}
)
SUPERDB

# ==============================================================================
# SECTION 4: Comment-based injection (Works with all plugins)
# Use this approach with the built-in Shell plugin
# ==============================================================================

# language=SuperDB
super -c "from data.json | where type == 'important'"

# Multi-line with comment injection
# language=SuperDB
super -c "from events.json
| where timestamp > now() - 1h
| sort -r timestamp
| head 100"

# ==============================================================================
# SECTION 5: Edge cases and complex patterns
# ==============================================================================

# Nested command substitution
echo "Count: $(super -c "from data.json | count()")"

# Pipeline with multiple super calls
super -c "from input.json | where active" | super -c "yield {count: count()}"

# Query with escaped characters
super -c "from data.json | where name == \"quoted\""

# Query with backticks (legacy command substitution)
COUNT=`super -c "from data.json | count()"`

# ANSI-C quoting (for complex escapes)
super -c $'from data.json | where name =~ /pattern\\d+/'

# Query in function
process_data() {
    local result
    result=$(super -c "from $1 | transform {id, name}")
    echo "$result"
}

# Query with here-string (<<<)
super -c "yield this" <<< '{"key": "value"}'

# ==============================================================================
# SECTION 6: Negative cases - these should NOT be injected
# ==============================================================================

# NOT a SuperDB query - regular echo
echo "from data.json | this is just a string"

# NOT a SuperDB query - grep with -c flag (different meaning)
grep -c "pattern" file.txt

# NOT a SuperDB query - curl with -c flag (cookies)
curl -c cookies.txt https://example.com

# NOT a SuperDB query - different command entirely
mysql -c "SELECT * FROM users"

# NOT SuperDB - heredoc without super command or recognized marker
cat <<"EOF"
This is just regular text
not SuperDB syntax
EOF

# NOT SuperDB - unquoted heredoc with variables (not valid injection host)
# Note: This would have variable expansion, breaking injection
# super -c <<EOF
# from $DATA_FILE | count()
# EOF
