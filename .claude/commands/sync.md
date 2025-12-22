# Sync SuperSQL Grammar

Fully automated synchronization of this IntelliJ plugin's grammar with the latest SuperSQL from brimdata/super.

**This command runs end-to-end automatically. No user confirmation required.**

## Execution Plan

Execute ALL steps below in sequence. Do NOT stop for confirmation. Fix any issues encountered automatically.

### Phase 1: Fetch Upstream Sources

Use `gh api` (not curl) to fetch files from brimdata/super main branch:

| File | Purpose | What to Extract |
|------|---------|-----------------|
| `compiler/parser/parser.peg` | Language syntax | Keywords, operators, types |
| `runtime/sam/expr/function/function.go` | Scalar functions | Built-in function names (not in grammar) |
| `runtime/sam/expr/agg/agg.go` | Aggregate functions | Aggregate function names (count, sum, avg, etc.) |
| `compiler/parser/valid.spq` | Test examples | Canonical valid SuperSQL queries |

Fetch each file:
```bash
gh api repos/brimdata/super/contents/compiler/parser/parser.peg --jq '.content' | base64 -d
gh api repos/brimdata/super/contents/runtime/sam/expr/function/function.go --jq '.content' | base64 -d
gh api repos/brimdata/super/contents/runtime/sam/expr/agg/agg.go --jq '.content' | base64 -d
gh api repos/brimdata/super/contents/compiler/parser/valid.spq --jq '.content' | base64 -d
```

Get the latest commit info:
```bash
gh api repos/brimdata/super/commits/main --jq '.sha'
```

### Phase 2: Extract Function Names from Go Files

**From `function.go`**: Look for function registrations like:
```go
zed.RegisterFunction("function_name", ...)
// or
"function_name": &SomeFunction{},
// or similar patterns
```

**From `agg.go`**: Look for aggregate registrations like:
```go
"count": &Count{},
"sum": &Sum{},
// etc.
```

Build a list of all function names - these should be recognized as built-in functions for syntax highlighting.

### Phase 3: Analyze Current Implementation

Read ALL of these files to understand current state:
- `src/main/java/org/clabs/superdb/SuperSQL.flex` (lexer)
- `src/main/java/org/clabs/superdb/supersql.bnf` (grammar)
- `src/main/java/org/clabs/superdb/SuperSQLSyntaxHighlighter.java`

### Phase 4: Identify Drift

Compare upstream sources with our implementation. Identify:
- **Missing keywords**: Keywords in parser.peg but not in our lexer
- **Missing functions**: Built-in functions from .go files not highlighted
- **Extra tokens**: In our lexer but removed from upstream
- **Changed patterns**: Tokens with different syntax or semantics
- **New operators**: Pipe operators added upstream
- **New types**: Primitive or PostgreSQL types added

### Phase 5: Update Lexer (SuperSQL.flex)

For EACH missing or changed token:
1. Add the token pattern to the lexer
2. Use case-insensitive patterns for keywords: `[Kk][Ee][Yy][Ww][Oo][Rr][Dd]`
3. Ensure proper ordering (longer matches before shorter)
4. Return the appropriate token type

For built-in functions:
- Consider adding them as recognized identifiers for semantic highlighting
- Or document them for future LSP/completion support

For removed tokens:
- Remove from lexer (but check if still used elsewhere first)

### Phase 6: Update Grammar (supersql.bnf)

1. Add new tokens to the `tokens = [...]` block
2. Update grammar rules to use new tokens
3. Add `{pin=N}` attributes for error recovery where appropriate
4. Ensure grammar rules match upstream semantics

### Phase 7: Update Syntax Highlighter

In `SuperSQLSyntaxHighlighter.java`:
- Add new tokens to appropriate categories:
  - `KEYWORD_KEYS` - SQL keywords (SELECT, FROM, WHERE, etc.)
  - `OPERATOR_KEYWORD_KEYS` - Pipe operators (FORK, SWITCH, SORT, etc.)
  - `TYPE_KEYWORD_KEYS` - Type names (uint8, string, etc.)
  - `CONSTANT_KEYS` - Constants (TRUE, FALSE, NULL, NaN, Inf)

### Phase 8: Generate Lexer and Parser

Run:
```bash
./gradlew generateLexer generateParser
```

If this fails, fix the errors in .flex or .bnf files and retry.

### Phase 9: Build

Run:
```bash
./gradlew build
```

If build fails:
- Read the error messages
- Fix compilation errors in generated or source files
- Retry until build succeeds

### Phase 10: Run Tests

Run:
```bash
./gradlew test
```

If tests fail:
1. Read the test failure output
2. For parser test failures:
   - Check if the test input is valid SuperSQL
   - Fix the grammar if the input should parse
   - Fix the test file if the input is invalid
3. For lexer test failures:
   - Update expected tokens in test assertions
4. For other failures:
   - Fix the underlying issue

### Phase 11: Regenerate Test Expected Outputs

After fixing any test issues, regenerate expected outputs:
```bash
./gradlew test -Didea.tests.overwrite.data=true
```

Then run tests again to confirm:
```bash
./gradlew test
```

Repeat until ALL tests pass.

### Phase 12: Validate Against SuperDB (if available)

If `super` binary is available:
```bash
./scripts/validate-test-files.sh
```

If validation fails for any test file:
1. Check if the syntax is valid upstream
2. Fix the test file OR fix our grammar
3. Retry validation

Skip this step if `super` is not installed.

### Phase 13: Update Examples

Review and update `examples/*.spq` files if new syntax was added.

### Phase 14: Final Verification

Run the complete build and test cycle one more time:
```bash
./gradlew clean build test
```

This MUST pass before committing.

### Phase 15: Commit

Create a commit with:
```bash
git add -A
git commit -m "$(cat <<'EOF'
Sync grammar with brimdata/super

Changes:
- [List all tokens/operators added]
- [List all tokens/operators removed]
- [List all syntax changes]
- [List all bug fixes made]

Upstream: https://github.com/brimdata/super
EOF
)"
```

### Phase 16: Push

Push to the current branch:
```bash
git push -u origin <current-branch>
```

If push fails due to network, retry up to 4 times with exponential backoff.

## Error Handling

- If ANY step fails, diagnose and fix the issue automatically
- Do NOT ask for user input - make reasonable decisions
- If a fix requires architectural changes, make them
- If stuck in a loop (same error 3+ times), try a different approach
- Log what you're doing so the user can review afterward

## Success Criteria

The sync is complete when:
1. All grammar changes from upstream are incorporated
2. `./gradlew clean build test` passes
3. Changes are committed and pushed
4. A summary of changes is provided to the user

## Output

After completion, provide a summary:
```
## Sync Complete

### Changes Made
- Added keywords: X, Y, Z
- Added functions: A, B, C
- Removed tokens: D, E
- Updated rules: ...
- Fixed bugs: ...

### Upstream Sources
- parser.peg commit: <hash>
- function.go commit: <hash>
- agg.go commit: <hash>

### Test Results
- All tests passing: Yes/No
- Tests added/modified: N

### Commit
- Hash: <commit-hash>
- Branch: <branch-name>
- Pushed: Yes/No
```
