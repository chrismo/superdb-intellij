#!/bin/bash
# Test basic super command patterns for language injection

# Simple -c flag
super -c "from data.json | where x > 5"

# With other flags before -c
super -s -c "from input.sup | sort ts"
super -f json -c "select * from data"
super -j -c "{a: 1, b: 2}"

# --command long form
super --command "from file.json | head 10"
super -s --command "count()"

# Multi-line queries
super -c "
    from data.json
    | where status == 'active'
    | sort -r created_at
    | head 100
"

# With stdin
echo '{"x": 1}' | super -c "where x > 0" -

# In command substitution
result=$(super -c "from data.json | count()")

# Heredoc style (if supported)
super -c "
type record = {
    id: string,
    value: int64
}
from input.json
| cast(this, <record>)
"
