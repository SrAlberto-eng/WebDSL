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

## Demo

[**Ver página generada en vivo →**](https://sralberto-eng.github.io/WebDSL/)

El archivo [`docs/index.html`](docs/index.html) es la salida real del traductor aplicado sobre `input.webdsl`.

## Ejemplo

**Entrada ([`input.webdsl`](src/main/java/com/alberto/webdsl/input.webdsl)):**

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
  	                imagen: "docs/foto.jpg"
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
        texto: "2025 AutoStock"
    }
}
```

**Salida ([`docs/index.html`](docs/index.html)):** página HTML completa con CSS y JavaScript embebidos, lista para abrir en el navegador o desplegar en GitHub Pages.

## Variables y datos dinámicos

Las variables declaradas en `variables:` se emiten como un bloque editable al inicio del `<body>`. El traductor infiere el tipo inicial según el uso:

- Si la variable aparece en un `repite(var)` → se inicializa como `[]`
- Si solo aparece en comparaciones o propiedades de texto → se inicializa como `""`

```html
<script>
  // ============================================
  //  DATOS DE LA PÁGINA — editar antes de servir
  // ============================================
  var productos = [];   // usada en repite()
  var usuario   = "";
</script>
```

Para poblar datos antes de abrir el HTML, edita ese bloque directamente:

```js
var productos = [1, 2, 3];   // 3 items → repite genera 3 tarjetas
var usuario   = "Admin";
```

Las propiedades que reciben una variable (`texto: miVar`) se resuelven automáticamente con un script runtime al final del `<body>` usando el atributo `data-var`.

## Tests

```bash
mvn test
```
