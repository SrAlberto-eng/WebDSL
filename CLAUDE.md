# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow rules

- **Branches:** Never work on `main` directly. Before starting any feature or fix, check the current branch with `git branch` and create a feature branch if needed (`git checkout -b feat/<name>`).
- **GitHub attribution:** All commits, PRs, and GitHub interactions are authored by the repository owner. Do not add co-author lines or mention AI involvement in commit messages, PR descriptions, or comments.
- **Changelog:** At the end of every session, update `CHANGELOG.md` under the `[Unreleased]` section with a summary of what changed.

## Build & Run

```bash
# Compile
mvn compile

# Run
mvn exec:java -Dexec.mainClass=com.alberto.webdsl.PruebaParser
```

Input file: `src/main/java/com/alberto/webdsl/input.webdsl`
Output file: `src/main/java/com/alberto/webdsl/output.html` (generated, Phase 8+)

There are no automated tests; verification is done by inspecting console output and the generated HTML file.

## Architecture

This is a **compiler pipeline** that translates a custom WebDSL language into a self-contained HTML file. All source lives under the package `com.alberto.webdsl`. The full 9-phase implementation spec is at `src/main/java/com/alberto/.docs/PLAN_IMPLEMENTACION.md`.

### Pipeline stages

```
input.webdsl → Lexer → Token[] → Parser → AST → Visitor → output.html
```

| Stage | Current class | Target class |
|---|---|---|
| Lexer | `lexer/PseudoLexer.java` | `lexer/WebDslLexer.java` |
| Parser | *(none yet)* | `parser/WebDslParser.java` |
| AST root | *(none yet)* | `astnodes/NodoPagina.java` |
| Code gen | *(none yet)* | `traductor/TraductorHtml.java` |
| Entry point | `PruebaParser.java` (placeholder) | `PruebaParser.java` (updated) |

The **symbol table** (`simbols/TablaSimbolos.java`) and **visitor interface** (`patronvisitor/ASTVisitor.java`) are kept across all phases.

### Key design patterns

- **Recursive descent parser** — hand-written, one method per grammar rule
- **Visitor pattern** — `ASTVisitor` interface; `TraductorHtml` will implement it to traverse the AST
- **Regex-based lexer** — ordered list of `Pattern` → `TipoToken` mappings

### AST node hierarchy

`Nodo` (abstract base) → concrete nodes. After Phase 1: `NodoSi`, `NodoRepite`, `NodoDeclaracion`, `NodoComparacion`, `NodoValor` all extend `Nodo` directly. Phase 4 will add `NodoElemento` as an intermediate base and introduce `NodoPagina`, `NodoComponente`, `NodoPropiedadTexto`, `NodoPropiedadEstilo`.

## Implementation status

Phases per `PLAN_IMPLEMENTACION.md`:

1. ✅ Base migration — package renamed to `com.alberto.webdsl`, old nodes/translators removed
2. `TipoToken` — WebDSL token catalog
3. `WebDslLexer`
4. AST nodes for WebDSL components
5. `ASTVisitor` interface (rewrite)
6. `WebDslParser`
7. `TraductorHtml` — emits self-contained HTML with embedded CSS/JS
8. `PruebaParser` integration (reads `input.webdsl`, writes `output.html`)
9. Cleanup & docs
