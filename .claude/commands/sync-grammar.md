# Sync SuperSQL Grammar with Upstream

Synchronize this IntelliJ plugin's grammar with the latest SuperSQL PEG grammar from the brimdata/super repository.

## Instructions

Perform a thorough update of the SuperSQL language support to match the current state of the upstream grammar. Follow these steps carefully:

### Step 1: Fetch Latest Grammar

1. Fetch the current PEG grammar from: `https://raw.githubusercontent.com/brimdata/super/main/compiler/parser/parser.peg`
2. Also fetch valid examples from: `https://raw.githubusercontent.com/brimdata/super/main/compiler/parser/valid.spq`
3. Save both to a temporary location for reference

### Step 2: Analyze Changes

Compare the fetched PEG grammar against our current implementation:

1. **Read current files:**
   - `src/main/java/org/clabs/superdb/SuperSQL.flex` (lexer)
   - `src/main/java/org/clabs/superdb/supersql.bnf` (grammar)
   - `src/main/java/org/clabs/superdb/SuperSQLSyntaxHighlighter.java` (highlighting)

2. **Identify differences:**
   - New keywords added upstream
   - Removed or renamed keywords
   - New operators or syntax
   - New token types (literals, etc.)
   - Changed grammar rules
   - New built-in functions

3. **Create a change report** listing:
   - Keywords to add
   - Keywords to remove
   - New operators
   - Grammar rule changes
   - Any breaking changes

### Step 3: Update Lexer (SuperSQL.flex)

For each new token identified:

1. Add token pattern to the lexer rules
2. Use case-insensitive patterns for keywords: `[Kk][Ee][Yy][Ww][Oo][Rr][Dd]`
3. Ensure proper ordering (longer matches before shorter)
4. Return appropriate token type

### Step 4: Update Grammar (supersql.bnf)

1. Add new tokens to the `tokens = [...]` block
2. Update grammar rules to use new tokens
3. Add new production rules as needed
4. Maintain proper precedence and associativity
5. Add `{pin=N}` for error recovery where appropriate

### Step 5: Update Syntax Highlighter

In `SuperSQLSyntaxHighlighter.java`:

1. Add new token types to appropriate color categories:
   - `KEYWORD_KEYS` - SQL keywords
   - `OPERATOR_KEYWORD_KEYS` - pipe operators
   - `TYPE_KEYWORD_KEYS` - type names
   - `CONSTANT_KEYS` - boolean/null/special values
   - `STRING_KEYS` - string literals
   - `NUMBER_KEYS` - numeric literals

2. Update `getTokenHighlights()` method with new cases

### Step 6: Update Tests

1. **Lexer tests** (`SuperSQLLexerTest.java`):
   - Add tests for new tokens
   - Update tests if tokens were renamed/removed

2. **Parser test data** (`src/test/testData/parser/`):
   - Add new .spq files for new syntax
   - Update existing files if syntax changed

3. **Integration tests**:
   - Ensure highlighting tests cover new tokens

### Step 7: Update Examples

Update example files in `examples/` directory:
- `basic_query.spq`
- `advanced_query.spq`

Add demonstrations of any significant new features.

### Step 8: Verify Build

Run these checks (if possible in the environment):
```bash
./gradlew generateLexer generateParser
./gradlew build
./gradlew test
```

### Step 9: Update Documentation

1. Update `EDITOR_TOOLING_SPEC.md` if needed
2. Add changelog entry noting the sync

### Step 10: Commit Changes

Create a commit with message format:
```
Sync grammar with brimdata/super@<commit-hash>

Changes:
- Added: <list new keywords/operators>
- Removed: <list removed items>
- Modified: <list modified rules>

Upstream reference: https://github.com/brimdata/super/commit/<hash>
```

## Important Notes

- **Don't break existing functionality** - be careful with removals
- **Preserve backwards compatibility** where possible
- **Test thoroughly** - grammar changes can have cascading effects
- **Check the CHANGELOG** in brimdata/super for context on changes
- **Reference upstream issues/PRs** when available

## Output

After completing the sync, provide:

1. Summary of all changes made
2. List of files modified
3. Any manual follow-up needed
4. Suggested test cases to verify
