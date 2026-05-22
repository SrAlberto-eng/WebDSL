# Manual de usuario — WebDSL

WebDSL es un lenguaje de marcado que describe la estructura de una página web
mediante componentes anidados. El traductor toma un archivo `.webdsl` y produce
un único archivo `.html` autocontenido listo para abrir en el navegador.

---

## Tabla de contenidos

1. [Requisitos y ejecución](#1-requisitos-y-ejecución)
2. [Estructura de un archivo WebDSL](#2-estructura-de-un-archivo-webdsl)
3. [Variables](#3-variables)
4. [Componentes](#4-componentes)
5. [Propiedades de contenido](#5-propiedades-de-contenido)
6. [Estilos](#6-estilos)
7. [Control de flujo](#7-control-de-flujo)
8. [Imágenes y rutas](#8-imágenes-y-rutas)
9. [Comentarios](#9-comentarios)
10. [Ejemplo completo](#10-ejemplo-completo)
11. [Poblar datos en el HTML generado](#11-poblar-datos-en-el-html-generado)
12. [Errores comunes](#12-errores-comunes)

---

## 1. Requisitos y ejecución

- Java 25
- Maven 3.x

```bash
# Compilar el proyecto
mvn compile

# Traducir input.webdsl → docs/index.html
mvn exec:java -Dexec.mainClass=com.alberto.webdsl.PruebaParser
```

El archivo de entrada es `src/main/java/com/alberto/webdsl/input.webdsl`.  
El archivo de salida es `docs/index.html`.

Para ejecutar los tests:

```bash
mvn test
```

---

## 2. Estructura de un archivo WebDSL

Todo archivo WebDSL define exactamente **una página** con la palabra reservada
`pagina`, un nombre, y un cuerpo entre llaves. El cuerpo siempre comienza con
la sección `variables:`.

```
pagina NombreDeLaPagina {

    variables: var1, var2

    # componentes y contenido aquí

}
```

**Reglas generales:**

- El nombre de la página no puede contener espacios.
- La sección `variables:` es obligatoria, incluso si está vacía no puede omitirse
  — si no necesitas variables, declara al menos una que no uses.
- Los componentes pueden anidarse libremente dentro de otros componentes.
- Las propiedades dentro de un bloque pueden separarse por salto de línea o por
  coma — ambas formas son válidas.

---

## 3. Variables

Las variables se declaran al inicio de la página y se pueden usar como valores
en propiedades de contenido y en expresiones de control de flujo.

```
variables: nombre, productos, usuario
```

- Los nombres siguen el patrón `[a-zA-Z_][a-zA-Z0-9_]*`.
- No pueden repetirse en la misma declaración.
- Se inicializan en el HTML generado como `""` (cadena vacía) o `[]` (array
  vacío) dependiendo de si aparecen en un `repite()` o no.

Para usar una variable como valor de una propiedad, escribe el nombre sin
comillas:

```
texto: nombre          # variable — se resuelve en tiempo de ejecución
texto: "Hola mundo"   # cadena literal — se emite directamente
```

---

## 4. Componentes

Los componentes son los bloques de construcción de una página. Se escriben con
su nombre seguido de un cuerpo entre llaves. Opcionalmente pueden recibir un
identificador único.

```
nombreComponente {
    # propiedades e hijos
}

nombreComponente miId {
    # el id se traduce al atributo id="miId" en el HTML
}
```

### Componentes disponibles

| WebDSL | HTML generado | Descripción |
|---|---|---|
| `encabezado { }` | `<header>` | Cabecera de la página |
| `nav { }` | `<nav>` | Barra de navegación |
| `seccion { }` | `<section>` | Sección de contenido |
| `seccion miId { }` | `<section id="miId">` | Sección con identificador |
| `lista { }` | `<ul>` | Lista de elementos |
| `lista miId { }` | `<ul id="miId">` | Lista con identificador |
| `tarjeta { }` | `<li class="tarjeta">` | Elemento de lista con estilo tarjeta |
| `columnas { }` | `<div class="columnas">` | Grilla de columnas automáticas |
| `boton { }` | `<button>` | Botón interactivo |
| `pie { }` | `<footer>` | Pie de página |
| `bloque { }` | `<div>` | Contenedor genérico |

### Ejemplo

```
encabezado {
    titulo: "Mi Sitio"
    nav {
        boton { texto: "Inicio", enlace: "#inicio" }
        boton { texto: "Contacto", enlace: "#contacto" }
    }
}

seccion inicio {
    estilo: relleno, centrado
    titulo: "Bienvenido"
    texto: "Descripción del sitio"
}
```

---

## 5. Propiedades de contenido

Las propiedades definen el contenido dentro de un componente. Se escriben como
`nombre: valor` donde el valor es una cadena entre comillas o el nombre de una
variable.

| Propiedad | HTML generado | Notas |
|---|---|---|
| `titulo: "Texto"` | `<h1>Texto</h1>` | Encabezado principal |
| `subtitulo: "Texto"` | `<h2>Texto</h2>` | Encabezado secundario |
| `texto: "Texto"` | `<p>Texto</p>` | Párrafo |
| `imagen: "ruta"` | `<img src="ruta">` | Ver sección [Imágenes](#8-imágenes-y-rutas) |
| `icono: "X"` | `<span class="icono">X</span>` | Texto o emoji como ícono |
| `enlace: "url"` | `onclick` en el botón padre | **Solo válido dentro de `boton`** |
| `estilo: clase1, clase2` | `class="clase1 clase2"` | Ver sección [Estilos](#6-estilos) |

### Valores literales vs variables

```
titulo: "AutoStock"       # literal — se escribe directo en el HTML
titulo: nombreSitio       # variable — se resuelve con el script de bindings
```

### La propiedad `enlace`

`enlace` solo puede usarse dentro de un `boton`. Genera un `onclick` que navega
a la URL indicada.

```
boton {
    texto: "Ver más"
    enlace: "#seccion-productos"
}
```

---

## 6. Estilos

La propiedad `estilo` aplica una o más clases CSS al componente que la contiene.
Se especifican separadas por coma.

```
seccion {
    estilo: fondo-primario, centrado, relleno-grande
}
```

### Estilos disponibles

| Clase | Efecto |
|---|---|
| `centrado` | Alinea el texto al centro |
| `izquierda` | Alinea el texto a la izquierda |
| `derecha` | Alinea el texto a la derecha |
| `fondo-oscuro` | Fondo oscuro (`#1a1a2e`) con texto claro |
| `fondo-claro` | Fondo claro (`#f5f7fa`) con texto oscuro |
| `fondo-primario` | Fondo azul (`#1d4ed8`) con texto blanco |
| `sombra` | Agrega sombra al elemento |
| `redondeado` | Esquinas redondeadas |
| `relleno` | Padding interno mediano (`2rem`) |
| `relleno-grande` | Padding interno grande (`4rem`) |
| `negrita` | Texto en negrita |
| `grande` | Texto más grande (`1.2rem`) |
| `chico` | Texto más pequeño (`0.8rem`) |
| `columnas` | Grilla automática con CSS Grid |

> `columnas` puede usarse tanto como nombre de componente (`columnas { }`) como
> valor en `estilo: columnas`. Ambos usos son válidos y producen el mismo CSS.

---

## 7. Control de flujo

WebDSL tiene dos estructuras de control que generan JavaScript embebido en el
HTML. Ambas requieren que las variables involucradas estén declaradas en
`variables:`.

### Condicional: `si / sino / fin-si`

```
si ( variable == "valor" ) entonces
    # contenido si la condición es verdadera
sino
    # contenido si la condición es falsa
fin-si
```

La rama `sino` es **obligatoria**, aunque puede estar vacía.

**Operadores relacionales disponibles:** `==` `!=` `<` `>` `<=` `>=`

**Ejemplos:**

```
si ( stock == 0 ) entonces
    texto: "Sin stock"
sino
    texto: "Disponible"
fin-si

si ( usuario == "admin" ) entonces
    boton { texto: "Panel de control", enlace: "/admin" }
sino
fin-si
```

El valor de comparación puede ser una cadena (`"valor"`), un número (`0`, `42`)
o una variable (sin comillas).

### Iteración: `repite / fin-repite`

```
repite ( nombreVariable )
    # contenido que se repite por cada elemento del array
fin-repite
```

`repite` itera sobre el array que se asigne a `nombreVariable` en el bloque de
datos del HTML generado. El cuerpo se repite tantas veces como elementos tenga
el array.

```
lista productos {
    estilo: columnas
    repite ( productos )
        tarjeta {
            estilo: sombra, redondeado
            subtitulo: "Producto"
            texto: "Descripción"
            boton { texto: "Ver detalle", enlace: "#" }
        }
    fin-repite
}
```

> El contenido dentro del `repite` es estático — todas las repeticiones muestran
> el mismo HTML. Para la demo, basta con poblar el array con N elementos para que
> aparezcan N tarjetas.

---

## 8. Imágenes y rutas

La propiedad `imagen` recibe una ruta relativa al `docs/index.html` generado, o
una URL completa.

```
imagen: "assets/foto.jpg"                    # ruta relativa a docs/
imagen: "https://picsum.photos/800/300"      # URL externa
```

Para que las imágenes locales funcionen, coloca los archivos dentro de
`docs/assets/` antes de abrir el HTML.

```
docs/
  index.html
  assets/
    foto.jpg
    logo.png
```

---

## 9. Comentarios

Los comentarios comienzan con `#` y se extienden hasta el final de la línea.
El traductor los descarta completamente.

```
# Este es un comentario
pagina MiSitio {
    variables: nombre   # también pueden ir al final de una línea
}
```

---

## 10. Ejemplo completo

```
# Sistema de inventario — AutoStock
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
        imagen: "assets/foto.jpg"
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
        texto: "2025 AutoStock"
    }
}
```

---

## 11. Poblar datos en el HTML generado

Al abrir `docs/index.html`, el bloque de datos al inicio del `<body>` tiene este
aspecto:

```html
<script>
  // ============================================
  //  DATOS DE LA PÁGINA — editar antes de servir
  // ============================================
  var productos = [];   // usada en repite()
  var usuario   = "";
</script>
```

Edita ese bloque directamente para ver la página con datos reales:

```html
<script>
  var productos = [1, 2, 3];   // genera 3 tarjetas
  var usuario   = "Admin";
</script>
```

El valor numérico de cada elemento del array no importa — lo que cuenta es la
cantidad. Con `[1, 2, 3]` aparecen tres tarjetas; con `[]` aparece el mensaje
de "No hay productos".

Las propiedades que usan variables (`texto: usuario`) se resuelven
automáticamente al cargar la página mediante un script de bindings que recorre
los elementos con el atributo `data-var`.

---

## 12. Errores comunes

### Error léxico

```
[ERROR LÉXICO] Unexpected token: @
```

El traductor encontró un carácter que no pertenece al lenguaje. Revisa el
archivo buscando caracteres especiales fuera de cadenas.

### Error sintáctico

```
[ERROR SINTÁCTICO] Se esperaba un token 'LLAVEDER' y se encontró 'TEXTO'
```

La estructura del archivo no respeta la gramática. Causas frecuentes:

| Mensaje | Causa probable |
|---|---|
| Se esperaba `LLAVEDER` | Falta cerrar `}` un componente |
| Se esperaba `FINSI` | Falta `fin-si` al cerrar un condicional |
| Se esperaba `FINREPITE` | Falta `fin-repite` al cerrar una iteración |
| Se esperaba `SINO` | El `si` no tiene rama `sino` |
| El símbolo X no ha sido declarado | Variable usada sin declararla en `variables:` |
| El símbolo X ya fue declarado | Variable repetida en la declaración |
| El id 'X' ya está en uso | Dos componentes con el mismo identificador |

### Error de generación

```
[ERROR DE GENERACIÓN] La propiedad 'enlace' solo es válida dentro de un 'boton'.
```

`enlace` se usó dentro de un componente que no es `boton`. Mueve esa propiedad
dentro de un `boton { }`.
