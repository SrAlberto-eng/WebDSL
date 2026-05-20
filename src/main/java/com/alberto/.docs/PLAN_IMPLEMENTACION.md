# Plan de implementación — WebDSL Translator

> **Proyecto:** Traductor de WebDSL (lenguaje propio orientado a componentes web) a HTML+CSS+JavaScript embebido en un solo archivo `.html`.
>
> **Lenguaje:** Java
>
> **Base:** Proyecto del curso `traductor` (lexer + parser + AST + visitor + tabla de símbolos en `com.alberto`). El árbol de directorios se copia íntegro a un proyecto nuevo y desde ahí se modifica.

---

## 0. Tabla de contenidos

1. [Visión general](#1-visión-general)
2. [Gramática formal](#2-gramática-formal)
3. [Tokens del lenguaje](#3-tokens-del-lenguaje)
4. [Arquitectura del traductor](#4-arquitectura-del-traductor)
5. [Mapeo DSL → HTML](#5-mapeo-dsl--html)
6. [Plan por fases](#6-plan-por-fases)
7. [CSS fijo del traductor](#7-css-fijo-del-traductor)
8. [Ejemplo de entrada y salida](#8-ejemplo-de-entrada-y-salida)
9. [Criterios de aceptación](#9-criterios-de-aceptación)

---

## 1. Visión general

**WebDSL** es un lenguaje de marcado propio que describe la estructura y comportamiento de una página web mediante componentes anidados (`encabezado`, `nav`, `seccion`, `lista`, `tarjeta`, `boton`, `pie`, etc.), propiedades (`titulo`, `texto`, `imagen`, `enlace`, `estilo`), control de flujo (`si … sino … fin-si`, `repite … fin-repite`) y declaración de variables.

El **traductor** toma un archivo `.webdsl` y emite un único archivo `.html` autocontenido con CSS fijo embebido en `<style>` y JavaScript embebido en `<script>` para las construcciones de control de flujo.

### Pipeline completo

```
archivo.webdsl
     │
     ▼
┌──────────────┐
│  WebDslLexer │  ── ArrayList<Token>
└──────────────┘
     │
     ▼
┌──────────────┐
│ WebDslParser │  ── NodoPagina (AST)
│              │  ── uso paralelo de TablaSimbolos
└──────────────┘
     │
     ▼
┌────────────────┐
│ TraductorHtml  │  ── String (HTML completo)
│  (ASTVisitor)  │
└────────────────┘
     │
     ▼
archivo.html
```

### Cumplimiento del curso

| Requisito | Cómo lo cubre el proyecto |
|---|---|
| Análisis lexicográfico | `WebDslLexer` con regex compuesto |
| Análisis sintáctico | `WebDslParser` descendente recursivo |
| Tabla de símbolos | `TablaSimbolos` (reutilizada del curso) |
| Generación de código | `TraductorHtml` (patrón Visitor) |

---

## 2. Gramática formal

```
<Programa>           -> pagina ID { <Cuerpo> }
<Cuerpo>             -> <Declaracion> <Secciones>
<Declaracion>        -> variables : <ListaVariables>
<ListaVariables>     -> ID , <ListaVariables> | ID
<Secciones>          -> <Elemento> <Secciones> | <Elemento>
<Elemento>           -> <Componente> | <ControlFlujo> | <Propiedad>
<Componente>         -> <TipoComponente> { <Secciones> }
                      | <TipoComponente> ID { <Secciones> }
<TipoComponente>     -> encabezado | nav | seccion | lista | tarjeta
                      | columnas | boton | pie | bloque
<Propiedad>          -> <PropiedadTexto> | <PropiedadEstilo>
<PropiedadTexto>     -> <NombrePropiedad> : <ValorPropiedad>
<NombrePropiedad>    -> texto | titulo | subtitulo | imagen | enlace | icono
<ValorPropiedad>     -> CADENA | ID
<PropiedadEstilo>    -> estilo : <ListaEstilos>
<ListaEstilos>       -> ID_ESTILO , <ListaEstilos> | ID_ESTILO
<ControlFlujo>       -> <Si> | <Repite>
<Si>                 -> si ( <Comparacion> ) entonces <Secciones> sino <Secciones> fin-si
<Repite>             -> repite ( ID ) <Secciones> fin-repite
<Comparacion>        -> <Valor> <OperadorRelacional> <Valor>
<Valor>              -> ID | CADENA | NUMERO
<OperadorRelacional> -> == | != | < | > | <= | >=
```

### Reglas semánticas (verificadas durante o después del parseo)

1. Toda variable usada en `<Valor>`, `repite (ID)`, o como `<ValorPropiedad>` debe haber sido declarada en `variables:`.
2. Una misma variable no puede aparecer dos veces en `<ListaVariables>`.
3. Cada `ID_ESTILO` en `<ListaEstilos>` debe pertenecer a la lista cerrada (sección 3.2).
4. Dos componentes con `ID` opcional no pueden tener el mismo identificador en todo el programa.

---

## 3. Tokens del lenguaje

### 3.1 Palabras reservadas (constantes en `TipoToken`)

| Constante | Lexema |
|---|---|
| `PAGINA` | `pagina` |
| `VARIABLES` | `variables` |
| `ESTILO` | `estilo` |
| `ENCABEZADO` | `encabezado` |
| `NAV` | `nav` |
| `SECCION` | `seccion` |
| `LISTA` | `lista` |
| `TARJETA` | `tarjeta` |
| `COLUMNAS_COMP` | `columnas` *(como componente)* |
| `BOTON` | `boton` |
| `PIE` | `pie` |
| `BLOQUE` | `bloque` |
| `TEXTO` | `texto` |
| `TITULO` | `titulo` |
| `SUBTITULO` | `subtitulo` |
| `IMAGEN` | `imagen` |
| `ENLACE` | `enlace` |
| `ICONO` | `icono` |
| `SI` | `si` |
| `ENTONCES` | `entonces` |
| `SINO` | `sino` |
| `FINSI` | `fin-si` |
| `REPITE` | `repite` |
| `FINREPITE` | `fin-repite` |

> **Nota:** la palabra `columnas` actúa como **componente** y también está en la lista de estilos válidos. El lexer la tokeniza como `COLUMNAS_COMP`; el parser/semántico decide según contexto. Es la única palabra ambigua y se resuelve por posición en el parser.

### 3.2 Identificadores de estilo (lista cerrada)

Token: `ID_ESTILO`. Lexemas válidos:

```
centrado      izquierda     derecha
fondo-oscuro  fondo-claro   fondo-primario
sombra        redondeado
relleno       relleno-grande
negrita       grande        pequeño
columnas
```

> El lexer reconoce un identificador de estilo solo cuando aparece en posición de lista de estilos. Para simplificar la implementación, se usará un único patrón regex que enumera explícitamente todos los valores válidos, y los identificadores comunes (`ID`) usan un patrón distinto.

### 3.3 Símbolos

| Constante | Lexema |
|---|---|
| `LLAVEIZQ` | `{` |
| `LLAVEDER` | `}` |
| `PARENTESISIZQ` | `(` |
| `PARENTESISDER` | `)` |
| `DOSPUNTOS` | `:` |
| `COMA` | `,` |
| `OPRELACIONAL` | `==`, `!=`, `<=`, `>=`, `<`, `>` |

### 3.4 Literales

| Constante | Patrón |
|---|---|
| `ID` | `[a-zA-Z_][a-zA-Z0-9_]*` |
| `CADENA` | `"[^"]*"` |
| `NUMERO` | `[0-9]+(\.[0-9]+)?` |

### 3.5 Ignorados

| Constante | Patrón | Acción |
|---|---|---|
| `ESPACIO` | `[ \t\r\n\f]+` | Descartar |
| `COMENTARIO` | `#[^\n]*` | Descartar |
| `ERROR` | `[^ \t\r\n\f]+` | Lanzar `LexicalException` |

---

## 4. Arquitectura del traductor

### 4.1 Estructura de paquetes (después de la migración)

```
com.alberto.webdsl
├── PruebaParser.java                  ← punto de entrada (Main)
├── input.webdsl                       ← archivo de ejemplo
├── output.html                        ← (generado en tiempo de ejecución)
│
├── lexer/
│   ├── WebDslLexer.java               ← (NUEVO, basado en PseudoLexer)
│   └── LexicalException.java          ← (sin cambios)
│
├── tokens/
│   ├── Token.java                     ← (sin cambios)
│   └── TipoToken.java                 ← (REESCRITO, nuevas constantes)
│
├── parser/
│   ├── WebDslParser.java              ← (NUEVO, basado en PseudoParser)
│   └── SyntaxException.java           ← (sin cambios)
│
├── astnodes/
│   ├── Nodo.java                      ← (sin cambios)
│   ├── NodoPagina.java                ← (NUEVO, reemplaza NodoPrograma)
│   ├── NodoElemento.java              ← (NUEVO, clase abstracta base)
│   ├── NodoDeclaracion.java           ← (sin cambios)
│   ├── NodoComponente.java            ← (NUEVO)
│   ├── NodoPropiedadTexto.java        ← (NUEVO)
│   ├── NodoPropiedadEstilo.java       ← (NUEVO)
│   ├── NodoSi.java                    ← (MODIFICADO, agregar rama "sino")
│   ├── NodoRepite.java                ← (MODIFICADO, simplificar a 1 variable)
│   ├── NodoComparacion.java           ← (sin cambios)
│   └── NodoValor.java                 ← (MODIFICADO, agregar tipo CADENA)
│
├── patronvisitor/
│   └── ASTVisitor.java                ← (REESCRITO, nuevos métodos visit)
│
├── traductor/
│   └── TraductorHtml.java             ← (NUEVO, implementa ASTVisitor)
│
└── simbols/
    ├── TablaSimbolos.java             ← (sin cambios)
    ├── Simbolo.java                   ← (sin cambios)
    ├── Tipo.java                      ← (sin cambios)
    ├── Variable.java                  ← (sin cambios)
    └── TipoIncorporado.java           ← (sin cambios)
```

### 4.2 Archivos a eliminar del proyecto base

```
astnodes/NodoPrograma.java              → reemplazado por NodoPagina
astnodes/NodoEnunciado.java             → reemplazado por NodoElemento
astnodes/NodoAsignacion.java            → no aplica al nuevo lenguaje
astnodes/NodoEscribir.java              → no aplica
astnodes/NodoLeer.java                  → no aplica
astnodes/NodoMientras.java              → no aplica
astnodes/NodoOperacion.java             → no aplica
astnodes/NodoExpresion.java             → no aplica
traductor/TraductorPy.java              → reemplazado por TraductorHtml
traductor/TraductorC.java               → no aplica
input.txt                               → reemplazado por input.webdsl
programa.py                             → archivo generado obsoleto
programa.c                              → archivo generado obsoleto
```

### 4.3 Archivos a conservar sin cambios

```
lexer/LexicalException.java
parser/SyntaxException.java
tokens/Token.java
astnodes/Nodo.java
astnodes/NodoComparacion.java
astnodes/NodoDeclaracion.java
simbols/*  (los 5 archivos)
```

---

## 5. Mapeo DSL → HTML

### 5.1 Componentes

| DSL | HTML generado |
|---|---|
| `pagina X { ... }` | `<!DOCTYPE html><html lang="es"><head>...<title>X</title>...</head><body>...</body></html>` |
| `encabezado { ... }` | `<header>...</header>` |
| `nav { ... }` | `<nav>...</nav>` |
| `seccion { ... }` | `<section>...</section>` |
| `seccion ID { ... }` | `<section id="ID">...</section>` |
| `lista { ... }` | `<ul>...</ul>` |
| `lista ID { ... }` | `<ul id="ID">...</ul>` |
| `tarjeta { ... }` | `<li class="tarjeta">...</li>` |
| `columnas { ... }` | `<div class="columnas">...</div>` |
| `boton { ... }` | `<button>...</button>` (si tiene `enlace`, agregar `onclick`) |
| `pie { ... }` | `<footer>...</footer>` |
| `bloque { ... }` | `<div>...</div>` |

### 5.2 Propiedades

| DSL | HTML generado |
|---|---|
| `titulo: "X"` | `<h1>X</h1>` |
| `subtitulo: "X"` | `<h2>X</h2>` |
| `texto: "X"` | `<p>X</p>` |
| `texto: variable` | `<p>${variable}</p>` (interpolado con `<script>`) |
| `imagen: "url"` | `<img src="url">` |
| `enlace: "url"` | atributo `onclick="location.href='url'"` en el botón padre |
| `icono: "X"` | `<span class="icono">X</span>` |
| `estilo: a, b, c` | atributo `class="a b c"` en el elemento padre |

### 5.3 Control de flujo

`si (...) entonces ... sino ... fin-si` y `repite (...) ... fin-repite` se traducen a bloques `<script>` que generan HTML dinámicamente. El traductor decide si emite código estático o dinámico según si involucran variables.

**Estrategia para `repite`:** genera un `<script>` con `forEach` que inserta el HTML de las secciones internas mediante template literals.

**Estrategia para `si`:** genera un `<script>` con `if/else` que inserta el HTML correspondiente.

---

## 6. Plan por fases

Cada fase tiene **archivos afectados**, **acción concreta**, y **criterio de verificación**. El orden importa: cada fase depende de la anterior compilando limpiamente.

---

### FASE 1 — Migración base y limpieza

**Objetivo:** Tener el proyecto base copiado, renombrado y compilando sin los archivos que ya no aplican.

**Acciones:**

1. Crear un proyecto Maven nuevo con groupId/artifactId apropiados.
2. Copiar el contenido de `com.alberto` al paquete `com.alberto.webdsl`.
3. Actualizar todas las declaraciones `package` y los `import` para reflejar el nuevo paquete.
4. Eliminar los archivos listados en §4.2 (excepto `TraductorPy.java` y `TraductorC.java`, que se eliminan en Fase 6 cuando ya exista `TraductorHtml`).
5. Eliminar todos los métodos `visit()` huérfanos de `ASTVisitor` que apunten a nodos eliminados.
6. Renombrar `input.txt` → `input.webdsl` (contenido vacío por ahora).

**Verificación:**

- `mvn compile` ejecuta sin errores.
- La clase `PruebaParser` está vacía o solo imprime un mensaje placeholder.

---

### FASE 2 — Tokens (`TipoToken`)

**Objetivo:** Definir el alfabeto del nuevo lenguaje.

**Archivo:** `tokens/TipoToken.java`

**Acción:** Reescribir el archivo completo. Las constantes finales son las de §3. Estructura idéntica al original:

```java
public static String PAGINA = "PAGINA";
public static String VARIABLES = "VARIABLES";
public static String ESTILO = "ESTILO";
// ... todas las palabras reservadas

public static String ENCABEZADO = "ENCABEZADO";
public static String NAV = "NAV";
// ... etc.

public static String LLAVEIZQ = "LLAVEIZQ";
public static String LLAVEDER = "LLAVEDER";
// ... símbolos

public static String ID = "ID";
public static String ID_ESTILO = "ID_ESTILO";
public static String CADENA = "CADENA";
public static String NUMERO = "NUMERO";
public static String OPRELACIONAL = "OPRELACIONAL";
public static String DOSPUNTOS = "DOSPUNTOS";
public static String COMA = "COMA";
public static String PARENTESISIZQ = "PARENTESISIZQ";
public static String PARENTESISDER = "PARENTESISDER";

public static String ESPACIO = "ESPACIO";
public static String COMENTARIO = "COMENTARIO";
public static String ERROR = "ERROR";
```

**Verificación:**

- `mvn compile` ejecuta sin errores.

---

### FASE 3 — Lexer (`WebDslLexer`)

**Objetivo:** Convertir texto `.webdsl` en una lista de tokens.

**Archivo nuevo:** `lexer/WebDslLexer.java`

**Acción:** Estructura idéntica a `PseudoLexer` pero con los patrones de WebDSL. Crítico el **orden** de los patrones en el `ArrayList<TipoToken>`:

```java
public WebDslLexer() {
    // 1. Literales con prioridad
    tipos.add(new TipoToken(TipoToken.NUMERO,    "[0-9]+(\\.[0-9]+)?"));
    tipos.add(new TipoToken(TipoToken.CADENA,    "\"[^\"]*\""));

    // 2. Operadores compuestos ANTES que los simples
    tipos.add(new TipoToken(TipoToken.OPRELACIONAL, "<=|>=|==|!=|<|>"));

    // 3. Símbolos simples
    tipos.add(new TipoToken(TipoToken.LLAVEIZQ,       "\\{"));
    tipos.add(new TipoToken(TipoToken.LLAVEDER,       "\\}"));
    tipos.add(new TipoToken(TipoToken.PARENTESISIZQ,  "\\("));
    tipos.add(new TipoToken(TipoToken.PARENTESISDER,  "\\)"));
    tipos.add(new TipoToken(TipoToken.DOSPUNTOS,      ":"));
    tipos.add(new TipoToken(TipoToken.COMA,           ","));

    // 4. Palabras reservadas (orden importante: las más específicas primero)
    tipos.add(new TipoToken(TipoToken.FINREPITE, "fin-repite"));
    tipos.add(new TipoToken(TipoToken.FINSI,     "fin-si"));
    tipos.add(new TipoToken(TipoToken.PAGINA,    "pagina"));
    tipos.add(new TipoToken(TipoToken.VARIABLES, "variables"));
    tipos.add(new TipoToken(TipoToken.ESTILO,    "estilo"));
    tipos.add(new TipoToken(TipoToken.ENCABEZADO,"encabezado"));
    tipos.add(new TipoToken(TipoToken.NAV,       "nav"));
    tipos.add(new TipoToken(TipoToken.SECCION,   "seccion"));
    tipos.add(new TipoToken(TipoToken.LISTA,     "lista"));
    tipos.add(new TipoToken(TipoToken.TARJETA,   "tarjeta"));
    // ... (todas las palabras reservadas de §3.1)
    tipos.add(new TipoToken(TipoToken.SI,        "si"));
    tipos.add(new TipoToken(TipoToken.ENTONCES,  "entonces"));
    tipos.add(new TipoToken(TipoToken.SINO,      "sino"));
    tipos.add(new TipoToken(TipoToken.REPITE,    "repite"));

    // 5. ID_ESTILO con lista cerrada (DEBE ir antes de ID porque algunos
    //    estilos como "centrado" cumplen el patrón de ID)
    tipos.add(new TipoToken(TipoToken.ID_ESTILO,
        "centrado|izquierda|derecha|" +
        "fondo-oscuro|fondo-claro|fondo-primario|" +
        "sombra|redondeado|relleno-grande|relleno|" +
        "negrita|grande|pequeño|columnas"));

    // 6. Identificador genérico (al final, captura lo no reservado)
    tipos.add(new TipoToken(TipoToken.ID, "[a-zA-Z_][a-zA-Z0-9_]*"));

    // 7. Ignorados
    tipos.add(new TipoToken(TipoToken.ESPACIO,    "[ \\t\\f\\r\\n]+"));
    tipos.add(new TipoToken(TipoToken.COMENTARIO, "#[^\\n]*"));

    // 8. Error
    tipos.add(new TipoToken(TipoToken.ERROR, "[^ \\t\\f\\r\\n]+"));
}
```

**Manejo del bloque `m.find()`** (en `analizar()`): mantener la lógica original, pero descartar también `COMENTARIO` igual que `ESPACIO`.

**Verificación:**

Crear un archivo `input.webdsl` con contenido mínimo:

```
pagina Hola {
    variables: nombre
    encabezado {
        titulo: "Hola Mundo"
        estilo: centrado, fondo-oscuro
    }
}
```

Y en `PruebaParser.main()`:

```java
WebDslLexer lexer = new WebDslLexer();
lexer.analizar(entrada);
for (Token t : lexer.getTokens()) System.out.println(t);
```

Debe imprimir la secuencia correcta de tokens, terminando sin lanzar `LexicalException`.

---

### FASE 4 — AST nodes

**Objetivo:** Definir las clases del árbol de sintaxis abstracta.

#### 4.1 `Nodo.java` (sin cambios)

```java
public abstract class Nodo {
    public abstract void accept(ASTVisitor visitor);
}
```

#### 4.2 `NodoElemento.java` (NUEVO, reemplaza `NodoEnunciado`)

```java
public abstract class NodoElemento extends Nodo { }
```

#### 4.3 `NodoPagina.java` (NUEVO, reemplaza `NodoPrograma`)

Campos:
- `String nombre` — nombre de la página
- `NodoDeclaracion declaracion`
- `List<NodoElemento> elementos`

#### 4.4 `NodoDeclaracion.java` (sin cambios)

Ya existe. Funciona igual: lista de nombres de variables.

#### 4.5 `NodoComponente.java` (NUEVO)

Campos:
- `String tipo` — uno de "encabezado", "nav", "seccion", "lista", "tarjeta", "columnas", "boton", "pie", "bloque"
- `String id` — opcional, puede ser `null`
- `List<NodoElemento> hijos` — contenido del componente

Extiende `NodoElemento`.

#### 4.6 `NodoPropiedadTexto.java` (NUEVO)

Campos:
- `String nombre` — "texto", "titulo", "subtitulo", "imagen", "enlace", "icono"
- `NodoValor valor` — cadena o variable

Extiende `NodoElemento`.

#### 4.7 `NodoPropiedadEstilo.java` (NUEVO)

Campos:
- `List<String> estilos` — nombres de clases CSS (`centrado`, `fondo-oscuro`, etc.)

Extiende `NodoElemento`.

#### 4.8 `NodoSi.java` (MODIFICADO)

Campos:
- `NodoComparacion comparacion`
- `List<NodoElemento> ramaEntonces`
- `List<NodoElemento> ramaSino`

> Importante: la gramática exige `sino` obligatorio. La lista puede estar vacía pero no `null`.

#### 4.9 `NodoRepite.java` (MODIFICADO)

Campos:
- `String variable` — variable a iterar
- `List<NodoElemento> cuerpo`

> Más simple que el original del curso: solo una variable, no rango.

#### 4.10 `NodoComparacion.java` (sin cambios)

Mantiene `izquierdo`, `derecho`, `operadorRelacional`. Los valores pueden ser `NodoValor` con cualquier tipo (ID, CADENA o NUMERO).

#### 4.11 `NodoValor.java` (MODIFICADO)

Cambiar `boolean esVariable` a un enum o constantes:

```java
public enum TipoValor { ID, CADENA, NUMERO }

private final TipoValor tipo;
private final String valor;
```

Eliminar la clase `NodoExpresion` (ya no existe jerarquía de expresiones aritméticas).

**Verificación:**

`mvn compile` ejecuta sin errores. Los nodos se pueden instanciar y acceden correctamente a sus getters.

---

### FASE 5 — ASTVisitor

**Objetivo:** Definir el contrato de visitantes para el nuevo árbol.

**Archivo:** `patronvisitor/ASTVisitor.java`

**Acción:** Reescribir con los nuevos métodos:

```java
public interface ASTVisitor {
    void visit(NodoPagina nodo);
    void visit(NodoDeclaracion nodo);
    void visit(NodoComponente nodo);
    void visit(NodoPropiedadTexto nodo);
    void visit(NodoPropiedadEstilo nodo);
    void visit(NodoSi nodo);
    void visit(NodoRepite nodo);
    void visit(NodoComparacion nodo);
    void visit(NodoValor nodo);
}
```

Y en cada `NodoXxx.java` el método `accept(ASTVisitor v)` invoca `v.visit(this)`.

**Verificación:**

`mvn compile` ejecuta sin errores y todas las clases del AST aceptan la interfaz.

---

### FASE 6 — Parser (`WebDslParser`)

**Objetivo:** Construir el AST a partir de la lista de tokens, aplicando la gramática de §2 y validando con tabla de símbolos.

**Archivo nuevo:** `parser/WebDslParser.java`

**Estructura general (idéntica patrón al `PseudoParser`):**

```java
public class WebDslParser {
    private ArrayList<Token> tokens;
    private int indiceToken = 0;
    private SyntaxException ex;
    private final TablaSimbolos ts;

    public WebDslParser(TablaSimbolos ts) { this.ts = ts; }

    public NodoPagina analizar(WebDslLexer lexer) throws SyntaxException {
        tokens = lexer.getTokens();
        NodoPagina pagina = Programa();
        if (pagina != null && indiceToken == tokens.size()) return pagina;
        if (ex == null) ex = new SyntaxException("Error de sintaxis");
        throw ex;
    }

    private boolean match(String nombre) { /* idéntico al original */ }
    private boolean currentToken(String nombre) { /* idéntico */ }

    // ... un método por cada producción de la gramática
}
```

#### Métodos del parser (uno por producción)

##### `Programa()`

```
<Programa> -> pagina ID { <Cuerpo> }
```

- `match(PAGINA)`
- `match(ID)` → guardar nombre
- `match(LLAVEIZQ)`
- llamar `Cuerpo()` → debe retornar `(NodoDeclaracion, List<NodoElemento>)`
- `match(LLAVEDER)`
- retornar `new NodoPagina(nombre, declaracion, elementos)`

##### `Cuerpo()`

```
<Cuerpo> -> <Declaracion> <Secciones>
```

Retornar una clase auxiliar interna o usar pares (`AbstractMap.SimpleEntry` o crear `record CuerpoResult(NodoDeclaracion, List<NodoElemento>)`).

##### `Declaracion()`

```
<Declaracion> -> variables : <ListaVariables>
```

- `match(VARIABLES)`
- `match(DOSPUNTOS)`
- `ListaVariables()` → registra cada variable en `ts.definir(new Variable(nombre, tipoCualquiera))`
- retornar `NodoDeclaracion`

##### `ListaVariables()`

```
<ListaVariables> -> ID , <ListaVariables> | ID
```

Idéntico patrón al `Variables()` original.

##### `Secciones()`

```
<Secciones> -> <Elemento> <Secciones> | <Elemento>
```

Loop while `Elemento() != null`. Debe consumir al menos uno.

##### `Elemento()`

```
<Elemento> -> <Componente> | <ControlFlujo> | <Propiedad>
```

Lookahead por el token actual:

```java
if (currentToken(SI))                 return Si();
if (currentToken(REPITE))             return Repite();
if (currentToken(ESTILO))             return PropiedadEstilo();
if (esNombrePropiedad(currentToken))  return PropiedadTexto();
if (esTipoComponente(currentToken))   return Componente();
return null;
```

Donde `esTipoComponente()` chequea si el token actual es uno de `ENCABEZADO`, `NAV`, `SECCION`, `LISTA`, `TARJETA`, `COLUMNAS_COMP`, `BOTON`, `PIE`, `BLOQUE`.

Y `esNombrePropiedad()` chequea `TEXTO`, `TITULO`, `SUBTITULO`, `IMAGEN`, `ENLACE`, `ICONO`.

##### `Componente()`

```
<Componente> -> <TipoComponente> { <Secciones> }
              | <TipoComponente> ID { <Secciones> }
```

- Match cualquier token de tipo componente, guardar el lexema como `tipo`.
- Opcional: `match(ID)` → guardar como `id`. Si lo hay, registrarlo para validar unicidad (regla semántica 4).
- `match(LLAVEIZQ)`
- `Secciones()` → lista de hijos
- `match(LLAVEDER)`
- retornar `new NodoComponente(tipo, id, hijos)`

##### `PropiedadTexto()`

```
<PropiedadTexto> -> <NombrePropiedad> : <ValorPropiedad>
```

- Match cualquier token de nombre de propiedad → guardar lexema
- `match(DOSPUNTOS)`
- `Valor()` → solo acepta `ID` o `CADENA` aquí (no `NUMERO`)
- retornar `new NodoPropiedadTexto(nombre, valor)`

##### `PropiedadEstilo()`

```
<PropiedadEstilo> -> estilo : <ListaEstilos>
```

- `match(ESTILO)`
- `match(DOSPUNTOS)`
- `ListaEstilos()` → `List<String>`
- retornar `new NodoPropiedadEstilo(estilos)`

##### `ListaEstilos()`

```
<ListaEstilos> -> ID_ESTILO , <ListaEstilos> | ID_ESTILO
```

Loop similar a `ListaVariables`, recolectando lexemas.

##### `Si()`

```
<Si> -> si ( <Comparacion> ) entonces <Secciones> sino <Secciones> fin-si
```

- `match(SI)`
- `match(PARENTESISIZQ)`
- `Comparacion()`
- `match(PARENTESISDER)`
- `match(ENTONCES)`
- `Secciones()` → rama entonces
- `match(SINO)`
- `Secciones()` → rama sino
- `match(FINSI)`
- retornar `new NodoSi(comparacion, ramaEntonces, ramaSino)`

##### `Repite()`

```
<Repite> -> repite ( ID ) <Secciones> fin-repite
```

- `match(REPITE)`
- `match(PARENTESISIZQ)`
- `match(ID)` → lexema = nombre de variable
- `ts.resolver(variable)` → valida que esté declarada
- `match(PARENTESISDER)`
- `Secciones()`
- `match(FINREPITE)`

##### `Comparacion()` y `Valor()`

Similares al original, pero `Valor()` ahora puede retornar `CADENA` además de `ID`/`NUMERO`. Si el valor es un `ID`, llamar `ts.resolver()` para validar declaración.

**Verificación:**

```java
WebDslLexer lexer = new WebDslLexer();
lexer.analizar(leerArchivo("input.webdsl"));
TablaSimbolos ts = new TablaSimbolos();
WebDslParser parser = new WebDslParser(ts);
NodoPagina pagina = parser.analizar(lexer);
System.out.println("Parseo exitoso: pagina " + pagina.getNombre());
```

Con un `.webdsl` válido completo (ver §8) no debe lanzar excepciones.

---

### FASE 7 — Generador HTML (`TraductorHtml`)

**Objetivo:** Recorrer el AST y emitir un archivo `.html` autocontenido.

**Archivo nuevo:** `traductor/TraductorHtml.java`

**Estructura general** (mismo patrón que `TraductorPy`):

```java
public class TraductorHtml implements ASTVisitor {
    private StringBuilder code;
    private int nivelIndentacion = 0;
    // Estado para acumular clases CSS del componente actual
    private List<String> estilosPendientes;
    // Estado para acumular onclick del componente actual (botones)
    private String enlacePendiente;

    private void indentar() { /* idéntico */ }

    public String generar(NodoPagina pagina) {
        code = new StringBuilder();
        pagina.accept(this);
        return code.toString();
    }

    // métodos visit() para cada nodo
}
```

#### `visit(NodoPagina)`

Emitir el esqueleto completo:

```html
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${pagina.nombre}</title>
<style>
/* CSS FIJO (ver §7) */
</style>
</head>
<body>
${visitar cada elemento del cuerpo}
</body>
</html>
```

#### `visit(NodoDeclaracion)`

Emitir un bloque `<script>` con las variables declaradas con valor por defecto:

```html
<script>
var nombre = "";
var productos = [];
// ...
</script>
```

**Nota:** El valor por defecto depende del uso. Como no hay tipos en WebDSL, se puede inicializar todo como `""` o detectar uso en `repite` para inferir array vs escalar. Para simplicidad, **inicializar todo como array vacío `[]`**. Si se usa como cadena, JS lo coerciona.

> **Decisión de diseño:** Si quieres simplificar al máximo, ignora la declaración y solo registra en la tabla de símbolos. El usuario final inyecta los valores en el HTML manualmente, o se agrega una sección de "datos iniciales" que el usuario edita. Discutir antes de implementar esta fase.

#### `visit(NodoComponente)`

1. Limpiar `estilosPendientes` y `enlacePendiente`.
2. **Primer pase** sobre `hijos`: recolectar propiedades de estilo y de enlace (para botones).
3. Mapear `tipo` a etiqueta HTML según §5.1.
4. Emitir apertura: `<tag class="..." id="..." onclick="...">` (solo los atributos relevantes).
5. **Segundo pase** sobre `hijos`: visitar componentes anidados y propiedades de texto.
6. Emitir cierre: `</tag>`.

```java
@Override
public void visit(NodoComponente nodo) {
    // Recolectar estilos e información especial
    List<String> clases = new ArrayList<>();
    String enlace = null;
    for (NodoElemento hijo : nodo.getHijos()) {
        if (hijo instanceof NodoPropiedadEstilo) {
            clases.addAll(((NodoPropiedadEstilo) hijo).getEstilos());
        }
        if (hijo instanceof NodoPropiedadTexto) {
            NodoPropiedadTexto pt = (NodoPropiedadTexto) hijo;
            if (pt.getNombre().equals("enlace")) {
                enlace = pt.getValor().getValor();
            }
        }
    }

    // Agregar clase implícita para tarjeta
    String tag = mapearTag(nodo.getTipo());  // "encabezado" -> "header"
    if (nodo.getTipo().equals("tarjeta")) clases.add(0, "tarjeta");
    if (nodo.getTipo().equals("columnas")) clases.add(0, "columnas");

    indentar();
    code.append("<").append(tag);
    if (nodo.getId() != null) code.append(" id=\"").append(nodo.getId()).append("\"");
    if (!clases.isEmpty()) {
        code.append(" class=\"")
            .append(String.join(" ", clases))
            .append("\"");
    }
    if (enlace != null && tag.equals("button")) {
        code.append(" onclick=\"location.href='").append(enlace).append("'\"");
    }
    code.append(">\n");

    // Visitar hijos NO-estilo y NO-enlace
    nivelIndentacion++;
    for (NodoElemento hijo : nodo.getHijos()) {
        if (hijo instanceof NodoPropiedadEstilo) continue;
        if (hijo instanceof NodoPropiedadTexto
            && ((NodoPropiedadTexto)hijo).getNombre().equals("enlace")) continue;
        hijo.accept(this);
    }
    nivelIndentacion--;

    indentar();
    code.append("</").append(tag).append(">\n");
}
```

Donde `mapearTag`:

```java
private String mapearTag(String tipo) {
    switch (tipo) {
        case "encabezado": return "header";
        case "nav":        return "nav";
        case "seccion":    return "section";
        case "lista":      return "ul";
        case "tarjeta":    return "li";
        case "columnas":   return "div";
        case "boton":      return "button";
        case "pie":        return "footer";
        case "bloque":     return "div";
        default: throw new IllegalStateException("Tipo desconocido: " + tipo);
    }
}
```

#### `visit(NodoPropiedadTexto)`

Mapear `nombre` a etiqueta:

```java
@Override
public void visit(NodoPropiedadTexto nodo) {
    String valor = nodo.getValor().getValor();
    boolean esVar = nodo.getValor().getTipo() == TipoValor.ID;
    indentar();
    switch (nodo.getNombre()) {
        case "titulo":
            code.append("<h1>").append(esVar ? "${" + valor + "}" : valor).append("</h1>\n");
            break;
        case "subtitulo":
            code.append("<h2>").append(esVar ? "${" + valor + "}" : valor).append("</h2>\n");
            break;
        case "texto":
            if (esVar) {
                code.append("<p><script>document.write(").append(valor).append(")</script></p>\n");
            } else {
                code.append("<p>").append(valor).append("</p>\n");
            }
            break;
        case "imagen":
            code.append("<img src=\"").append(valor).append("\">\n");
            break;
        case "icono":
            code.append("<span class=\"icono\">").append(valor).append("</span>\n");
            break;
        case "enlace":
            // Se manejó en el visit(NodoComponente) padre
            break;
    }
}
```

#### `visit(NodoPropiedadEstilo)`

Vacío. Las clases se recolectan en el `visit` del componente padre.

#### `visit(NodoSi)`

Emitir un `<script>` con `if/else`:

```java
@Override
public void visit(NodoSi nodo) {
    indentar();
    code.append("<script>\n");
    indentar();
    code.append("if (");
    nodo.getComparacion().accept(this);
    code.append(") {\n");
    // Las ramas emiten HTML mediante document.write
    nivelIndentacion++;
    code.append(emitirHtmlComoDocumentWrite(nodo.getRamaEntonces()));
    nivelIndentacion--;
    indentar();
    code.append("} else {\n");
    nivelIndentacion++;
    code.append(emitirHtmlComoDocumentWrite(nodo.getRamaSino()));
    nivelIndentacion--;
    indentar();
    code.append("}\n</script>\n");
}
```

Donde `emitirHtmlComoDocumentWrite()` recorre la lista de elementos, genera su HTML en un sub-traductor, escapa, y los envuelve en `document.write(\`...\`)`.

#### `visit(NodoRepite)`

```java
@Override
public void visit(NodoRepite nodo) {
    indentar();
    code.append("<script>\n");
    indentar();
    code.append("(").append(nodo.getVariable())
        .append(" || []).forEach(function(item) {\n");
    nivelIndentacion++;
    code.append(emitirHtmlComoDocumentWrite(nodo.getCuerpo()));
    nivelIndentacion--;
    indentar();
    code.append("});\n</script>\n");
}
```

#### `visit(NodoComparacion)` y `visit(NodoValor)`

Solo emiten texto plano (sin nueva línea, usados dentro de expresiones JS):

```java
@Override
public void visit(NodoComparacion nodo) {
    nodo.getIzquierdo().accept(this);
    code.append(" ").append(nodo.getOperadorRelacional()).append(" ");
    nodo.getDerecho().accept(this);
}

@Override
public void visit(NodoValor nodo) {
    switch (nodo.getTipo()) {
        case CADENA: code.append("\"").append(nodo.getValor()).append("\""); break;
        case NUMERO: code.append(nodo.getValor()); break;
        case ID:     code.append(nodo.getValor()); break;
    }
}
```

**Verificación:**

Con el `input.webdsl` de ejemplo de §8:

```java
String html = new TraductorHtml().generar(pagina);
Files.writeString(Path.of("output.html"), html);
```

Abrir `output.html` en el navegador. Debe verse correctamente, con el CSS aplicado.

---

### FASE 8 — Main (`PruebaParser`) y ejemplo

**Objetivo:** Integrar todo, leer `input.webdsl`, escribir `output.html`.

**Archivo:** `PruebaParser.java`

```java
public class PruebaParser {
    public static void main(String[] args) throws Exception {
        String entrada = Files.readString(Path.of("input.webdsl"));

        // Léxico
        WebDslLexer lexer = new WebDslLexer();
        lexer.analizar(entrada);
        System.out.println("*** Análisis léxico ***");
        for (Token t : lexer.getTokens()) System.out.println(t);

        // Sintáctico + Tabla de símbolos
        TablaSimbolos ts = new TablaSimbolos();
        WebDslParser parser = new WebDslParser(ts);
        NodoPagina pagina = parser.analizar(lexer);
        System.out.println("\n*** Tabla de símbolos ***");
        for (Simbolo s : ts.getSimbolos()) System.out.println(s);

        // Generación
        String html = new TraductorHtml().generar(pagina);
        Files.writeString(Path.of("output.html"), html);
        System.out.println("\n*** HTML generado en output.html ***");
    }
}
```

**Verificación:**

`mvn exec:java -Dexec.mainClass=com.alberto.webdsl.PruebaParser` produce `output.html` válido y abrible en navegador.

---

### FASE 9 — Limpieza y documentación

1. Eliminar `TraductorPy.java` y `TraductorC.java`.
2. Verificar que no haya imports rotos.
3. Crear `README.md` con instrucciones de compilación y ejecución.
4. Crear `MANUAL.md` con descripción de la gramática y ejemplos.
5. Probar con 2-3 archivos `.webdsl` distintos.

---

## 7. CSS fijo del traductor

Este bloque CSS se emite **idéntico en todos los archivos generados**. Se incrusta dentro de `<style>` en el `<head>` del HTML.

```css
* { box-sizing: border-box; margin: 0; padding: 0; }
html { scroll-behavior: smooth; }

body {
  font-family: system-ui, sans-serif;
  font-size: 16px;
  line-height: 1.6;
  color: #1a1a1a;
  background: #ffffff;
}

h1 { font-size: 2rem;   font-weight: 700; line-height: 1.2; }
h2 { font-size: 1.4rem; font-weight: 600; line-height: 1.3; }
p  { font-size: 1rem;   color: inherit; }
img { max-width: 100%; display: block; }

header  { display: flex; align-items: center; justify-content: space-between; padding: 0 1.5rem; min-height: 60px; }
nav     { display: flex; align-items: center; gap: 0.5rem; }
section { display: block; width: 100%; }
footer  { display: block; padding: 1.5rem; }
ul      { list-style: none; padding: 0; }

li.tarjeta {
  display: flex; flex-direction: column;
  background: #ffffff; border: 1px solid #e2e8f0;
  overflow: hidden;
}
li.tarjeta h2 { padding: 1rem 1rem 0.25rem; }
li.tarjeta p  { padding: 0 1rem 1rem; color: #555; font-size: 0.9rem; }
li.tarjeta button { margin: 0 1rem 1rem; align-self: flex-start; }

button {
  font-family: inherit; font-size: 0.9rem; font-weight: 500;
  padding: 0.5rem 1.2rem;
  border: 1.5px solid #1a1a1a; background: transparent; color: #1a1a1a;
  cursor: pointer;
}
button:hover { background: #1a1a1a; color: #fff; }
nav button { border: none; background: transparent; color: inherit; padding: 0.4rem 0.9rem; }
nav button:hover { background: rgba(255,255,255,0.12); color: inherit; }

.centrado  { text-align: center; }
.izquierda { text-align: left;   }
.derecha   { text-align: right;  }

.fondo-oscuro   { background: #1a1a2e; color: #f0f0f0; }
.fondo-claro    { background: #f5f7fa; color: #1a1a1a; }
.fondo-primario { background: #1d4ed8; color: #ffffff; }

.sombra     { box-shadow: 0 4px 16px rgba(0,0,0,0.10); }
.redondeado { border-radius: 10px; }

.relleno        { padding: 2rem 1.5rem; }
.relleno-grande { padding: 4rem 1.5rem; }

.negrita  { font-weight: 700; }
.grande   { font-size: 1.2rem; }
.pequeño  { font-size: 0.8rem; }

.columnas {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1.25rem;
}

@media (max-width: 768px) {
  header { flex-direction: column; align-items: flex-start; gap: 0.5rem; padding: 1rem; }
  nav { flex-wrap: wrap; }
}
```

**Implementación:** guardar este bloque en una constante `String CSS_FIJO` dentro de `TraductorHtml`, o leerlo de un recurso `src/main/resources/webdsl.css`.

---

## 8. Ejemplo de entrada y salida

### 8.1 Entrada: `input.webdsl`

```
# Página de ejemplo para AutoStock
pagina AutoStock {

    variables: productos, usuario

    encabezado {
        estilo: fondo-oscuro
        titulo: "AutoStock"
        subtitulo: "Sistema de Inventario"
        nav {
            boton { texto: "Inicio",     enlace: "#inicio" }
            boton { texto: "Inventario", enlace: "#inv" }
        }
    }

    seccion inicio {
        estilo: fondo-primario, centrado, relleno-grande
        titulo: "Control total"
        texto: "Importa facturas XML automáticamente"
        boton { texto: "Empezar", enlace: "#inv" }
    }

    seccion inv {
        estilo: relleno
        subtitulo: "Inventario"

        lista catalogo {
            estilo: columnas
            repite ( productos )
                tarjeta {
                    estilo: sombra, redondeado
                    subtitulo: "Producto"
                    texto: "Stock disponible"
                    boton { texto: "Ver detalle", enlace: "#" }
                }
            fin-repite
            si ( productos == 0 ) entonces
                texto: "No hay productos"
            sino
                texto: "Productos cargados"
            fin-si
        }
    }

    pie {
        estilo: fondo-oscuro, centrado
        texto: "© 2025 AutoStock"
    }
}
```

### 8.2 Salida esperada: `output.html`

Un archivo `.html` autocontenido con:

- `<!DOCTYPE html>` y estructura HTML5 completa
- `<title>AutoStock</title>`
- Bloque `<style>` con el CSS fijo de §7
- `<header class="fondo-oscuro">` con `<h1>`, `<h2>` y `<nav>` con `<button>`s
- `<section id="inicio" class="fondo-primario centrado relleno-grande">` con contenido
- `<section id="inv">` con `<ul id="catalogo" class="columnas">` que contiene un `<script>` con `forEach` y `<script>` con `if/else`
- `<footer class="fondo-oscuro centrado">`

---

## 9. Criterios de aceptación

### Funcionales

- [ ] El programa lee `input.webdsl`, ejecuta las 4 fases del traductor, y produce `output.html`.
- [ ] El HTML generado abre correctamente en Chrome/Firefox sin errores en la consola.
- [ ] Los componentes anidados generan estructura HTML anidada equivalente.
- [ ] Las clases de estilo se aplican como atributo `class="..."` y el CSS las reconoce.
- [ ] `si … sino … fin-si` produce un `<script>` con `if/else` válido.
- [ ] `repite (var) … fin-repite` produce un `<script>` con `forEach` válido.
- [ ] Un programa con variable no declarada lanza `SyntaxException` con mensaje claro.
- [ ] Un programa con sintaxis incorrecta lanza `SyntaxException` con mensaje claro.
- [ ] Un programa con estilo desconocido lanza error semántico.

### No funcionales

- [ ] El proyecto compila con `mvn compile` sin warnings críticos.
- [ ] El bloque CSS del traductor es **idéntico** en todos los archivos generados (verificable con `diff`).
- [ ] No hay archivos de la versión anterior (`NodoLeer`, `TraductorPy`, etc.).

### Entregables del curso

- [ ] Código fuente completo en repositorio Git.
- [ ] `MANUAL.md` con instrucciones, gramática y ejemplos.
- [ ] Video de demostración mostrando un `.webdsl` traducido y abriéndose en el navegador.

---

## 10. Decisiones pendientes (discutir antes de implementar)

Los siguientes puntos se identificaron durante el diseño y requieren confirmación antes de codificarlos:

1. **Inicialización de variables en `visit(NodoDeclaracion)`** — ¿Inicializar como `[]`, como `""`, o pedirle al usuario que edite el `<script>` generado? (Ver Fase 7, sección `visit(NodoDeclaracion)`.)
2. **Mensajes de error del lexer y parser** — ¿Incluir número de línea y columna? El parser actual no lleva esa información. Agregarla requiere modificar `Token` para guardar `linea` y `columna`.
3. **Validación semántica de estilos** — La gramática ya restringe los lexemas válidos vía regex. ¿Es suficiente, o se quiere una verificación adicional en el parser?
4. **Inferencia entre `texto: variable` y `texto: "literal"`** — Confirmado en §5.2: si es ID se interpola con `<script>`, si es CADENA se emite directo. Sin coerción de tipos.

---

*Fin del plan. Cualquier ambigüedad pendiente debe resolverse al inicio de la fase correspondiente, no durante la implementación.*
