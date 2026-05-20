# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [0.1.1] - 2026-05-20

### Changed
- `CLAUDE.md` excluido del repositorio vía `.gitignore` (permanece solo en disco local)

## [0.1.0] - 2026-05-20

### Added
- `CLAUDE.md` with build commands, architecture overview, and workflow rules
- `README.md` with project description, usage, grammar summary, and phase status table
- `CHANGELOG.md`
- `.gitignore` for Maven build artifacts and IDE files

### Changed
- Migrated all Java source from `com.alberto` to `com.alberto.webdsl` (Phase 1)
- `PruebaParser` replaced with placeholder (`WebDSL Translator — en construcción`)
- `ASTVisitor` trimmed to only the nodes still in use: `NodoComparacion`, `NodoDeclaracion`, `NodoRepite`, `NodoSi`, `NodoValor`
- `NodoSi`, `NodoRepite`, `NodoDeclaracion` now extend `Nodo` directly (intermediate `NodoEnunciado` removed)
- `NodoValor` now extends `Nodo` directly (`NodoExpresion` removed)
- `input.txt` renamed to `input.webdsl` (empty placeholder)

### Removed
- `NodoPrograma`, `NodoEnunciado`, `NodoExpresion`, `NodoAsignacion`, `NodoEscribir`, `NodoLeer`, `NodoMientras`, `NodoOperacion`
- `TraductorC`, `TraductorPy`, `PseudoParser`
- Generated files: `programa.c`, `programa.py`
