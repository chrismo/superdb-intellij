# Sync SuperSQL Grammar

Synchronize this IntelliJ plugin's grammar with the latest SuperSQL from brimdata/super.

## Prerequisites

The `super` binary is required for validating test files:

```bash
# Install via asdf
asdf plugin add superdb https://github.com/chrismo/asdf-superdb.git
asdf install superdb latest
asdf global superdb latest

# Verify
super --version
```

## Instructions

### Step 1: Fetch Upstream Grammar

1. Fetch PEG grammar: `https://raw.githubusercontent.com/brimdata/super/main/compiler/parser/parser.peg`
2. Fetch valid examples: `https://raw.githubusercontent.com/brimdata/super/main/compiler/parser/valid.spq`

### Step 2: Analyze Drift

Read current implementation:
- `src/main/java/org/clabs/superdb/SuperSQL.flex` (lexer)
- `src/main/java/org/clabs/superdb/supersql.bnf` (grammar)
- `src/main/java/org/clabs/superdb/SuperSQLSyntaxHighlighter.java`

Compare and identify:
- **Missing tokens**: In upstream but not in our lexer
- **Extra tokens**: In our lexer but not in upstream
- **Changed patterns**: Tokens with different syntax

### Step 3: Validate Test Files

Run `./scripts/validate-test-files.sh` to check test files against real SuperDB parser.

Use `./scripts/claude-superdb-test.sh` to test individual files. See `.claude/commands/use-tools.md` for usage examples.

### Step 4: Report Before Changes

Provide a drift report:

```
## Grammar Drift Report

### Status: [IN SYNC | OUT OF SYNC]

### Missing Tokens (need to add)
- TOKEN_NAME: description

### Removed Tokens (may need to remove)
- TOKEN_NAME: description

### Changed Syntax
- TOKEN_NAME: old_pattern → new_pattern

### Test File Validation
- Passed: N files
- Failed: N files (list with errors)

### Recommendation
[Summary of changes needed]
```

**Stop here and confirm with user before making changes.**

### Step 5: Update Lexer (SuperSQL.flex)

For each new token:
1. Add token pattern (case-insensitive for keywords: `[Kk][Ee][Yy]`)
2. Ensure proper ordering (longer matches first)
3. Return appropriate token type

### Step 6: Update Grammar (supersql.bnf)

1. Add new tokens to `tokens = [...]` block
2. Update grammar rules
3. Add `{pin=N}` for error recovery where appropriate

### Step 7: Update Syntax Highlighter

In `SuperSQLSyntaxHighlighter.java`, add tokens to appropriate categories:
- `KEYWORD_KEYS` - SQL keywords
- `OPERATOR_KEYWORD_KEYS` - pipe operators
- `TYPE_KEYWORD_KEYS` - type names

### Step 8: Update Tests

1. **Lexer tests** (`SuperSQLLexerTest.java`): Add/update token tests
2. **Parser test data** (`src/test/testData/parser/`): Add/fix .spq files
3. **Validate**: Run `./scripts/validate-test-files.sh` again
4. **Regenerate expected outputs**: `./gradlew test -Didea.tests.overwrite.data=true`

### Step 9: Update Examples

Update `examples/*.spq` with new syntax demonstrations.

### Step 10: Verify Build

```bash
./gradlew generateLexer generateParser
./gradlew build
./gradlew test
```

### Step 11: Commit

```
Sync grammar with brimdata/super@<commit-hash>

Changes:
- Added: <list>
- Removed: <list>
- Modified: <list>

Upstream: https://github.com/brimdata/super/commit/<hash>
```

## Notes

- Don't break existing functionality
- Test thoroughly - grammar changes cascade
- Check brimdata/super CHANGELOG for context
