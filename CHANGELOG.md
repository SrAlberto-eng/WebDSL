# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

## [0.6.0] - 2026-05-20

### Added
- `WebParser` — analizador sintáctico descendente recursivo; un método por producción gramatical
- Validación semántica integrada: `variables:` registradas en `TablaSimbolos`, uso de variables no declaradas y de IDs de componentes duplicados lanza `SyntaxException`
- Tests unitarios con JUnit 5 (22 casos: 14 válidos, 8 de error)
- JUnit Jupiter 5.10.2 agregado a `pom.xml`

### Changed
- Gramática extendida: coma como separador opcional entre elementos dentro de un bloque
- `ListaEstilos()` acepta `COLUMNAS_COMP` además de `ID_ESTILO` para resolver la ambigüedad de la palabra `columnas`
- `PruebaParser` ejecuta análisis léxico y sintáctico, imprime tabla de símbolos

## [0.5.0] - 2026-05-20

### Added
- `ASTVisitor` — interfaz completa con los 9 métodos `visit()`: `NodoPagina`, `NodoDeclaracion`, `NodoComponente`, `NodoPropiedadTexto`, `NodoPropiedadEstilo`, `NodoSi`, `NodoRepite`, `NodoComparacion`, `NodoValor`
- Todos los nodos AST implementan `accept(ASTVisitor v)` para soportar el patrón visitor

> Completado durante la fase de nodos AST para mantener el proyecto compilable en todo momento.

## [0.4.0] - 2026-05-20

### Added
- `NodoElemento` — clase abstracta base para elementos del cuerpo de página o componente
- `NodoPagina` — nodo raíz del AST con nombre, declaración y lista de elementos
- `NodoComponente` — nodo para componentes con tipo, id opcional e hijos
- `NodoPropiedadTexto` — nodo para propiedades de contenido (titulo, texto, imagen, etc.)
- `NodoPropiedadEstilo` — nodo para la propiedad `estilo` con lista de clases CSS
- Documentación Javadoc en todos los archivos del proyecto

### Changed
- `NodoSi` — agrega rama `sino` obligatoria; listas cambian a `List<NodoElemento>`
- `NodoRepite` — simplificado: solo variable de iteración y cuerpo
- `NodoDeclaracion` — extiende `NodoElemento`
- `NodoValor` — `boolean esVariable` reemplazado por enum `TipoValor {ID, CADENA, NUMERO}`
- `ASTVisitor` — métodos `visit` actualizados para cubrir todos los nodos del AST

## [0.3.0] - 2026-05-20

### Changed
- `WebLexer` completado con todos los patrones WebDSL en el orden correcto
- `sino` posicionado antes que `si`; `relleno-grande` antes que `relleno`
- `COMENTARIO` descartado en `analizar()` igual que `ESPACIO`
- Método `grupo()` interno para compatibilidad de nombres de grupos regex con Java
- `PruebaParser` actualizado para invocar `WebLexer` e imprimir tokens
- `input.webdsl` con ejemplo funcional

## [0.2.0] - 2026-05-20

### Added
- `TipoToken` con el alfabeto completo de WebDSL
- `WebLexer` como analizador léxico WebDSL

### Removed
- `PseudoLexer`

## [0.1.1] - 2026-05-20

### Changed
- `CLAUDE.md` excluido del repositorio vía `.gitignore`

## [0.1.0] - 2026-05-20

### Added
- Estructura inicial del proyecto: paquete `com.alberto.webdsl`, pipeline léxico-sintáctico base, tabla de símbolos, patrón visitor
- `README.md`, `CHANGELOG.md`, `.gitignore`
