# SuperDB Plugin for JetBrains IDEs 

In Feb 2025 I messed around with this just a little bit, then didn't do
anything else.

In Dec 2025 with Claude Code in da house, I started unleashing it for some
full-on vibe coding sessions in the cloud, cuz I didn't have much to lose with
giving it a shot.

[Custom Language Support Tutorial](https://plugins.jetbrains.com/docs/intellij/custom-language-support-tutorial.html)

## Why does Grammar-Kit use a .bnf file for a PEG parser?

Grammar-Kit is a JetBrains tool for IntelliJ plugin development:
- .bnf file → generates parser + PSI (Program Structure Interface) classes
- .flex file → generates lexer via JFlex

**1. Grammar-Kit Uses a Modified BNF Syntax to Represent PEGs**

*   The core point is that Grammar-Kit doesn't strictly adhere to *pure* BNF. It
    uses BNF-like notation but interprets it with PEG semantics.

*   The `.bnf` file extension is more of a historical artifact or a practical
    choice for the plugin's architecture. It leverages existing BNF parsing and
    editing support within IntelliJ. Think of it as "BNF-inspired" or
    "BNF-extended" syntax.

*   The Grammar-Kit plugin then takes this modified BNF and generates a parser
    based on PEG principles.

**2. Key Differences Between Grammar-Kit's BNF and Standard BNF**

*   **Prioritized Choice:** Grammar-Kit treats the `|` (choice) operator in a
    BNF rule as a *prioritized* choice, just like PEGs. This means the order of
    alternatives matters. The parser will try them in the order they are
    defined, and the first successful match wins. This is the **biggest
    difference** from standard BNF.

*   **Syntactic Predicates:** Grammar-Kit supports `&` (and-predicate) and `!`
    (not-predicate), which are common in PEGs but not in standard BNF.

*   **No True Ambiguity:** Because of the prioritized choice, the grammars you
    define in Grammar-Kit are effectively unambiguous, even though the
    underlying notation might look like it could produce ambiguities in a
    traditional BNF setting.

**3. Why Use BNF Notation at All?**

*   **Familiarity:** BNF is a well-known and widely understood notation for
    describing grammars.

*   **Tooling:** IntelliJ IDEA has built-in support for BNF files, including
    syntax highlighting, validation, and structure views.  Grammar-Kit likely
    leverages these features.

*   **Evolutionary Approach:** It's possible Grammar-Kit started with a more
    traditional BNF focus and evolved to incorporate PEG features, keeping the
    `.bnf` extension for compatibility.

**In Simple Terms:**

Grammar-Kit uses a `.bnf` file for its grammar definition, but it *interprets*
the contents of that file as a PEG. The `|` operator is treated as a prioritized
choice, and syntactic predicates are supported, which are key characteristics of
PEGs.

**Analogy:**

Think of it like this: you might use a text editor designed for writing Python
code to also write JavaScript. The editor understands the basic syntax of both,
but you're still writing JavaScript, even though the editor's name implies it's
for Python.

**In Summary**

Don't get too hung up on the `.bnf` extension. Focus on the *semantics* of the
grammar you're writing. Understand that Grammar-Kit interprets the grammar rules
in a PEG-like way, especially regarding prioritized choice. The documentation
for Grammar-Kit itself is the best reference for understanding how its BNF
dialect works.

Citations:
[1] https://github.com/JetBrains/Grammar-Kit

## Planned Features

### Editing & Navigation
- [ ] Syntax highlighting improvements
- [ ] Code completion / autocomplete
- [ ] Go to definition
- [ ] Find usages / references
- [ ] Structure view (outline)
- [ ] Breadcrumbs

### Code Quality
- [ ] Error highlighting / live validation
- [ ] Quick fixes / intentions
- [ ] Code inspections

### Refactoring
- [ ] Rename refactoring
- [ ] Extract variable/function

### Formatting
- [ ] Auto-formatting / code style
- [ ] Brace matching improvements

### Documentation
- [ ] Quick documentation popup
- [ ] Parameter hints

### Language Injection

Full SuperDB/SuperSQL language support inside shell scripts, including syntax highlighting, code completion, and error checking.

#### BashSupport Pro (Recommended)

[BashSupport Pro](https://www.bashsupport.com/) provides the best experience with **automatic injection** - no comments required!

**Automatic Detection (No Comments Needed):**

```bash
# String-based queries - automatic injection!
super -c "from data.json | where status == 'active'"
super --command "from events.json | count()"

# Multi-line strings work too!
super -c "from events.json
| where timestamp > now() - 1h
| sort -r timestamp
| head 100"

# Heredocs with QUOTED markers (disable variable expansion)
super -c <<"EOF"
from events.json
| where level == 'error'
| sort -r timestamp
EOF

# Marker-based heredocs (inject even without super command)
cat <<"SUPERSQL" | super
from data.json
| where status == 'active'
| count()
SUPERSQL
```

**Recognized Heredoc Markers:**
- `SUPERSQL`, `SUPER`, `SPQ`, `ZQ`, `SUPERDB` (case-insensitive)
- These markers trigger injection regardless of the command name

**Important: Heredoc Quoting**

Heredocs must use **quoted markers** (`<<"EOF"` or `<<'EOF'`) for injection to work. Unquoted heredocs (`<<EOF`) allow variable expansion, which conflicts with language injection.

```bash
# ✅ Works - quoted marker disables variable expansion
super -c <<"EOF"
from data.json | count()
EOF

# ❌ Won't inject - unquoted marker allows variables
super -c <<EOF
from $DATA_FILE | count()
EOF
```

**Status:**
- [x] Automatic `super -c "..."` detection
- [x] Automatic `super --command "..."` detection
- [x] Multi-line string injection
- [x] Heredoc injection (quoted markers)
- [x] Marker-based heredoc injection (SUPERSQL, SUPER, SPQ, ZQ, SUPERDB)
- [x] Comment/manual injection via IntelliLang

#### Built-in Shell Plugin

The built-in Shell plugin (`com.intellij.sh`) has a **technical limitation** - its PSI elements don't implement `PsiLanguageInjectionHost`, which IntelliJ's injection framework requires.

**Workaround:** Use `# language=SuperDB` comment annotations:

```bash
# language=SuperDB
super -c "from data.json | where type == 'important'"
```

**Limitation:** The comment-based approach only works for single-line strings immediately following the comment. Multi-line support is limited.

### Other
- [ ] Run configurations (execute SuperDB queries from IDE)
- [ ] File templates
- [ ] Live templates / snippets

## Development

### Building

```bash
./build.sh compile    # Compile the plugin
./build.sh test       # Run tests
./build.sh package    # Build distributable zip
./build.sh ide        # Launch test IDE sandbox
```

### Releasing

Releases follow the SuperDB version with a patch number: `0.51222.0` (SuperDB 0.51222, patch 0).

```bash
# 1. Create release (runs tests, creates tag)
./build.sh release 0.51222.0

# 2. Push tag to trigger GitHub Actions
git push --tags
```

GitHub Actions will:
- Build the plugin with bundled LSP binaries
- Run tests and verification
- Create a GitHub Release with the artifact

### Version Format

- **Pre-release era** (current): `0.5MMDD.patch` (e.g., `0.51222.0`)
- **Post-release era** (future): `X.Y.Z.patch` (e.g., `1.0.0.0`)

### Utility Commands

```bash
./build.sh show-ide-versions    # Show configured IDE versions for testing
./build.sh update-ide-versions  # Auto-update to latest IDE versions
./build.sh lsp                  # Download latest LSP binaries
./build.sh install-local        # Install to local IDE (macOS)
```
