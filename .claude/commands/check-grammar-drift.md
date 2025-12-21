# Check Grammar Drift

Check if this plugin's grammar is out of sync with the upstream brimdata/super repository.

## Instructions

Perform a read-only analysis comparing our grammar with upstream. Do NOT make any changes.

### Step 1: Fetch Upstream Grammar

Fetch the current PEG grammar from:
- `https://raw.githubusercontent.com/brimdata/super/main/compiler/parser/parser.peg`

### Step 2: Extract Upstream Tokens

From the PEG grammar, extract:
1. All keyword definitions (case-insensitive tokens like `SELECT`, `FROM`, etc.)
2. All operator keywords (pipe operators like `FORK`, `SWITCH`, etc.)
3. All type keywords (`int64`, `string`, etc.)
4. All special operators (`|>`, `:=`, `::`, etc.)
5. Any new literal patterns

### Step 3: Compare with Current Implementation

Read our current lexer: `src/main/java/org/clabs/superdb/SuperSQL.flex`

Compare and identify:
- **Missing tokens**: In upstream but not in our lexer
- **Extra tokens**: In our lexer but not in upstream (possibly removed)
- **Changed patterns**: Tokens with different syntax

### Step 4: Report

Provide a drift report in this format:

```
## Grammar Drift Report

### Status: [IN SYNC | OUT OF SYNC]

### Missing Tokens (need to add)
- TOKEN_NAME: description

### Removed Tokens (may need to remove)
- TOKEN_NAME: description

### Changed Syntax
- TOKEN_NAME: old_pattern → new_pattern

### New Grammar Rules
- rule_name: description

### Recommendation
[Summary of what needs to be done]

Run `/sync-grammar` to apply these changes.
```

## Notes

- This is a **read-only** check - do not modify any files
- Focus on significant differences, not whitespace or comments
- Note any breaking changes that would affect existing .spq files
