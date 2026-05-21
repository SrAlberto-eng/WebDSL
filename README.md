# WebDSL Translator

Traductor de **WebDSL** a HTML. Toma un archivo `.webdsl` que describe la estructura de una página web mediante componentes y genera un único archivo `.html` autocontenido con CSS y JavaScript embebidos.

## Requisitos

- Java 21
- Maven 3.x

## Uso

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.alberto.webdsl.PruebaParser
```

Archivo de entrada: `src/main/java/com/alberto/webdsl/input.webdsl`  
Archivo de salida: `src/main/java/com/alberto/webdsl/output.html`

## El lenguaje WebDSL

Una página se describe con `pagina`, una sección de variables y una lista de componentes anidados. Los componentes aceptan propiedades de contenido y estilo.

### Componentes disponibles

| Componente | HTML generado |
|---|---|
| `encabezado` | `<header>` |
| `nav` | `<nav>` |
| `seccion [id]` | `<section id="...">` |
| `lista [id]` | `<ul id="...">` |
| `tarjeta` | `<li class="tarjeta">` |
| `columnas` | `<div class="columnas">` |
| `boton` | `<button>` |
| `pie` | `<footer>` |
| `bloque` | `<div>` |

### Propiedades

| Propiedad | Resultado |
|---|---|
| `titulo: "Texto"` | `<h1>` |
| `subtitulo: "Texto"` | `<h2>` |
| `texto: "Texto"` | `<p>` |
| `imagen: "url"` | `<img src="...">` |
| `enlace: "url"` | `onclick` en el botón padre |
| `icono: "X"` | `<span class="icono">` |
| `estilo: clase1, clase2` | atributo `class` en el elemento padre |

### Control de flujo

```
si (variable == "valor") entonces
  ...
sino
  ...
fin-si

repite (lista)
  ...
fin-repite
```

### Estilos disponibles

`centrado` `izquierda` `derecha` `fondo-oscuro` `fondo-claro` `fondo-primario`  
`sombra` `redondeado` `relleno` `relleno-grande` `negrita` `grande` `chico` `columnas`

## Ejemplo

**Entrada (`input.webdsl`):**

```
pagina MiSitio {
  variables: nombre, descripcion

  encabezado {
    estilo: fondo-primario, centrado
    titulo: "Bienvenido"
    subtitulo: "Mi sitio de ejemplo"
  }

  seccion principal {
    estilo: relleno
    texto: "Contenido principal"
    boton {
      texto: "Ver más"
      enlace: "#principal"
    }
  }

  pie {
    estilo: centrado, fondo-oscuro
    texto: "2025 MiSitio"
  }
}
```

**Salida (`output.html`):** página HTML completa con estilos embebidos lista para abrir en el navegador.

## Tests

```bash
mvn test
```
