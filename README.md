# WebDSL Translator

Traductor de **WebDSL** a HTML+CSS+JavaScript. Toma un archivo `.webdsl` que describe la estructura y comportamiento de una página web mediante componentes (`encabezado`, `nav`, `seccion`, `lista`, `tarjeta`, `boton`, `pie`, etc.) y genera un único archivo `.html` autocontenido.

Proyecto desarrollado para el curso de compiladores — Java 21, Maven.

## Requisitos

- Java 21
- Maven 3.x

## Uso

```bash
# Compilar
mvn compile

# Ejecutar
mvn exec:java -Dexec.mainClass=com.alberto.webdsl.PruebaParser
```

Editar `src/main/java/com/alberto/webdsl/input.webdsl` con el programa WebDSL a traducir. El HTML generado se escribe en `src/main/java/com/alberto/webdsl/output.html`.

## Ejemplo de entrada

```
pagina MiSitio {
  variables: titulo, descripcion

  encabezado {
    titulo: "Bienvenido"
    estilo: fondo-primario, centrado
  }

  seccion principal {
    texto: "Contenido principal"
  }

  pie {
    texto: "© 2025"
  }
}
```

## Gramática (resumen)

```
<Programa>    -> pagina ID { <Cuerpo> }
<Cuerpo>      -> variables: <IDs> <Elementos>
<Elemento>    -> <Componente> | <Propiedad> | <ControlFlujo>
<Componente>  -> encabezado|nav|seccion|lista|... [ID] { <Elementos> }
<Propiedad>   -> titulo|texto|imagen|enlace|estilo : <Valor>
<ControlFlujo>-> si (...) entonces ... sino ... fin-si
              |  repite (ID) ... fin-repite
```

Especificación completa en `src/main/java/com/alberto/.docs/PLAN_IMPLEMENTACION.md`.

## Estado

| Fase | Descripción | Estado |
|------|-------------|--------|
| 1 | Migración base (`com.alberto.webdsl`) | ✅ |
| 2 | `TipoToken` WebDSL | ✅ |
| 3 | `WebDslLexer` | ⬜ |
| 4 | Nodos AST WebDSL | ⬜ |
| 5 | `ASTVisitor` reescrito | ⬜ |
| 6 | `WebDslParser` | ⬜ |
| 7 | `TraductorHtml` | ⬜ |
| 8 | Integración `PruebaParser` | ⬜ |
| 9 | Limpieza y documentación | ⬜ |
