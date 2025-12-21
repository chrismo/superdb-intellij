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
