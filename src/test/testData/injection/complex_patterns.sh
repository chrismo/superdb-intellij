#!/bin/bash
# Test complex and edge case patterns

# join() as function (not SQL JOIN)
super -c "
    unnest this.oauth_config.scopes.bot
    | collect(this)
    | join(this, ',')
"

# is() function
super -c "where this is not null"
super -c "is(<record>)"

# Cast expressions with :: operator
super -c "{port: 443::uint16, size: 1024::uint64}"
super -c "54321::uint16"

# IN operator with array expression
super -c "where x in arr"
super -c "where x in this.items"
super -c "where x not in excluded"

# IPv6 network literals
super -c "where net == 2001:db8::/32"

# Type keywords as field names
super -c "{ip: 192.168.1.50, duration: 5m, time: now()}"

# Parenthesized expressions in records
super -c "{
    a: (1 + 2),
    b: (now() - 1h),
    c: (x * y / z)
}"

# UNNEST with INTO
super -c "unnest arr into (x, y, z)"

# Nested function calls
super -c "len(split(name, ','))"

# Complex aggregation
super -c "
    from events
    | where ts > now() - 1h
    | summarize count() by status
    | sort -r count
"
