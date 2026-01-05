# SuperDB Editor Tooling Specification

This document outlines the architecture and implementation plan for SuperDB/SuperSQL editor support across multiple platforms.

## Overview

SuperSQL is an extended SQL with pipe syntax used by SuperDB. This spec covers the tooling needed for syntax highlighting, code intelligence, and editor integration.

## Repository Structure

```
brimdata/
├── super                    # Core DB (existing)
│   └── cmd/super-lsp/       # LSP server (future, optional)
│
├── superdb-lsp              # (renamed from superdb-syntaxes)
│   ├── grammar/             # TextMate grammar (.tmLanguage.json)
│   ├── lsp/                 # Go LSP server wrapping super/compiler
│   ├── vscode/              # VS Code extension (or separate repo)
│   └── docs/                # Editor setup guides
│
└── superdb-intellij         # IntelliJ plugin (this repo)
                             # LSP4IJ integration + native fallback
```

### Alternative: Separate VS Code Repo

If VS Code extension grows complex or needs independent releases:

```
brimdata/
├── super
├── superdb-lsp              # Grammar + LSP server
├── superdb-vscode           # VS Code extension (bundles grammar + LSP)
└── superdb-intellij         # IntelliJ plugin
```

## Components

### 1. TextMate Grammar (`superdb-lsp/grammar/`)

**Purpose**: Basic syntax highlighting for GitHub, VS Code, Sublime, Monaco, etc.

**File**: `supersql.tmLanguage.json`

**Scopes to define**:
- `keyword.control.sql` - SELECT, FROM, WHERE, etc.
- `keyword.operator.pipe` - FORK, SWITCH, SORT, HEAD, etc.
- `keyword.declaration` - CONST, FN, LET, OP, TYPE
- `storage.type` - int64, string, float64, duration, ip, etc.
- `constant.language` - true, false, null, NaN, Inf
- `constant.numeric` - integers, floats, durations, timestamps
- `constant.other.ip` - IP addresses and networks
- `string.quoted` - single, double, backtick, raw, f-strings
- `string.regexp` - /pattern/
- `comment.line` - -- comment
- `comment.block` - /* comment */
- `punctuation.pipe` - |, |>
- `punctuation.definition` - (), [], {}, |[, |{

**Source of truth**: `brimdata/super/compiler/parser/parser.peg`

**Deliverable**: Submit to [github/linguist](https://github.com/github/linguist) for GitHub syntax highlighting.

---

### 2. LSP Server (`superdb-lsp/lsp/`)

**Purpose**: Semantic code intelligence for all editors.

**Language**: Go (to reuse `brimdata/super/compiler/parser`)

**Protocol**: LSP 3.17+ via JSON-RPC over stdio

**Capabilities (phased)**:

| Phase | Capability | Description |
|-------|------------|-------------|
| 1 | `textDocument/publishDiagnostics` | Parse errors and warnings |
| 1 | `textDocument/didOpen/Save/Close` | Document sync |
| 2 | `textDocument/completion` | Keyword and function completions |
| 2 | `textDocument/hover` | Type information on hover |
| 3 | `textDocument/definition` | Go to definition for functions/consts |
| 3 | `textDocument/references` | Find all references |
| 4 | `textDocument/formatting` | Code formatting |
| 4 | `textDocument/semanticTokens` | Semantic highlighting |

**Binary name**: `super-lsp` (or integrate into `super` CLI as `super lsp`)

**Distribution**:
- GitHub releases (binaries for linux/mac/windows)
- Bundled in editor extensions
- Optional: `go install github.com/brimdata/superdb-lsp@latest`

---

### 3. VS Code Extension (`superdb-lsp/vscode/` or `superdb-vscode`)

**Purpose**: Full SuperSQL support in VS Code.

**Contents**:
```
vscode/
├── package.json           # Extension manifest
├── syntaxes/
│   └── supersql.tmLanguage.json  # Copied/linked from grammar/
├── language-configuration.json   # Brackets, comments, etc.
├── src/
│   └── extension.ts       # LSP client startup
└── bin/
    └── super-lsp          # Bundled LSP binary (per-platform)
```

**Features**:
- Syntax highlighting (TextMate grammar)
- Bracket matching, auto-close
- Comment toggling (-- and /* */)
- Code folding
- LSP features (diagnostics, completions, hover)

**Publishing**: VS Code Marketplace as `brimdata.supersql`

---

### 4. IntelliJ Plugin (`superdb-intellij`)

**Purpose**: Full SuperSQL support in IntelliJ-based IDEs.

**Current state**: Hybrid implementation with native Grammar-Kit + LSP4IJ integration.

**Architecture**:
- **Native layer** (fast, always available):
  - Syntax highlighting via JFlex lexer
  - Brace matching
  - Code folding
  - Commenting
- **LSP layer** (via LSP4IJ):
  - Diagnostics
  - Completions
  - Hover
  - Go to definition

**Why hybrid?**:
- Native highlighting is faster (no process startup)
- LSP provides semantic features that are hard to replicate
- Graceful degradation if LSP unavailable

**LSP Integration**:
The plugin automatically fetches LSP binaries from `chrismo/superdb-lsp` releases.

```
src/main/java/org/clabs/superdb/lsp/
├── SuperSQLLanguageServerFactory.java  # LSP connection provider
├── SuperSQLLspServerSupportProvider.java  # Enablement logic
└── SuperSQLLspSettings.java  # User configuration
```

**LSP Binary Resolution** (in order):
1. System property `supersql.lsp.path`
2. Bundled binary in plugin resources
3. `super-lsp` in system PATH

**Automation**:
- `./gradlew downloadLsp` - Download LSP for current platform
- `./gradlew downloadLspAll` - Download LSP for all platforms
- GitHub Actions: `sync-lsp.yml` - Auto-update when new LSP releases

**Publishing**: JetBrains Marketplace

---

### 5. Other Editors

No dedicated repos needed—just documentation.

**Neovim** (`nvim-lspconfig`):
```lua
require('lspconfig').super_lsp.setup{
  cmd = { "super-lsp" },
  filetypes = { "supersql" },
  root_dir = function() return vim.fn.getcwd() end,
}
```

**Zed** (`settings.json`):
```json
{
  "languages": {
    "SuperSQL": {
      "language_servers": ["super-lsp"]
    }
  }
}
```

**Helix** (`languages.toml`):
```toml
[[language]]
name = "supersql"
scope = "source.supersql"
file-types = ["spq"]
language-server = { command = "super-lsp" }
```

---

## Implementation Sessions

### Phase 1: Foundations (Parallel)

**Session A: TextMate Grammar** (superdb-lsp repo)
- Update `supersql.tmLanguage.json` to match current PEG
- Test in VS Code
- Submit Linguist PR for GitHub highlighting
- ~1 session

**Session B: LSP Server** (superdb-lsp repo)
- Scaffold Go module
- Wrap `brimdata/super/compiler/parser`
- Implement Phase 1 capabilities (diagnostics)
- ~2-3 sessions

### Phase 2: Editor Integration (After Phase 1)

**Session C: VS Code Extension** (superdb-lsp/vscode or superdb-vscode)
- Create extension scaffold
- Bundle grammar + LSP
- Publish to Marketplace
- ~1 session

**Session D: IntelliJ LSP Integration** (superdb-intellij)
- Add LSP4IJ dependency
- Configure LSP client
- Keep native as fallback
- ~1 session

**Session E: Documentation**
- Neovim/Zed/Helix setup guides
- README updates
- ~0.5 session

---

## Dependency Graph

```
parser.peg (source of truth)
     │
     ├──────────────────┬────────────────────┐
     ▼                  ▼                    ▼
TextMate Grammar    LSP Server         IntelliJ Native
     │                  │               (Grammar-Kit)
     │                  │                    │
     ▼                  │                    │
GitHub Linguist        │                    │
VS Code highlighting    │                    │
     │                  │                    │
     └────────┬─────────┴────────────────────┘
              ▼
       Editor Plugins
    (VS Code, IntelliJ, etc.)
```

---

## File Extension & Language ID

| Attribute | Value |
|-----------|-------|
| File extension | `.spq` |
| Language ID | `supersql` |
| Language name | SuperSQL |
| TextMate scope | `source.supersql` |
| MIME type | `text/x-supersql` |

---

## Open Questions

1. **LSP location**: Should `super-lsp` live in `brimdata/super` (same release cycle) or `superdb-lsp` (independent)?

2. **VS Code bundling**: Bundle LSP binary in extension, or require separate install?

3. **Semantic tokens**: Should LSP provide semantic highlighting, or rely on TextMate grammar?

4. **ZSON support**: SuperDB also has ZSON data format—should it share the same tooling or be separate?

---

## References

- [SuperDB Documentation](https://superdb.org)
- [SuperSQL Language Intro](https://superdb.org/super-sql/intro.html)
- [Parser PEG Grammar](https://github.com/brimdata/super/blob/main/compiler/parser/parser.peg)
- [LSP Specification](https://microsoft.github.io/language-server-protocol/)
- [GitHub Linguist](https://github.com/github/linguist)
- [LSP4IJ for IntelliJ](https://github.com/redhat-developer/lsp4ij)
- [TextMate Grammar Guide](https://macromates.com/manual/en/language_grammars)
