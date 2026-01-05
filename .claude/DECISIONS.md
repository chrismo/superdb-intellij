# Architecture Decisions

## Grammar Sync Strategy (2024-12-22)

### Context

The IntelliJ plugin has two layers of syntax support:

1. **LSP (bundled binary)** - Diagnostics, completion, hover from superdb-lsp
2. **Native parser (GrammarKit)** - Syntax highlighting, code folding, brace matching

The native parser needs to stay in sync with upstream brimdata/super grammar (parser.peg).

### Options Considered

| Approach | Effort | Reliability | Notes |
|----------|--------|-------------|-------|
| Claude via `/sync` | Low | Good | Can handle judgment calls, edge cases |
| Diff-reporter script | Medium | Medium | Reports changes, human applies them |
| Full PEG→BNF converter | High | Brittle | Complex due to lexer split, case-insensitivity, error recovery attributes |

### Challenges with Full Automation

1. **Lexer/Parser split**: PEG combines lexing/parsing; GrammarKit needs separate JFlex + BNF
2. **Case insensitivity**: Keywords need `[Ss][Ee][Ll][Ee][Cc][Tt]` patterns in lexer
3. **Error recovery**: `{pin=N}` and `{recoverWhile=...}` attributes are manual additions
4. **Syntax highlighter mapping**: Token → color category requires judgment

### Decision

**Use Claude via `/sync` command** for now. The grammar doesn't change frequently, and Claude can handle:
- Identifying what changed upstream
- Making judgment calls (keyword vs identifier)
- Fixing test failures
- Updating syntax highlighter categories

### Future Considerations

- If grammar churn increases, consider building a diff-reporter
- Could explore TextMate grammar (from superdb-lsp) as simpler alternative
- LSP could eventually replace native parser for most features
