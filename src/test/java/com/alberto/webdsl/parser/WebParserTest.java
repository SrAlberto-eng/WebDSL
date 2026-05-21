package com.alberto.webdsl.parser;

import com.alberto.webdsl.astnodes.*;
import com.alberto.webdsl.lexer.LexicalException;
import com.alberto.webdsl.lexer.WebLexer;
import com.alberto.webdsl.simbols.TablaSimbolos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebParserTest {

    // ── Helper ───────────────────────────────────────────────────────────────

    private NodoPagina parsear(String fuente) throws LexicalException, SyntaxException {
        WebLexer lexer = new WebLexer();
        lexer.analizar(fuente);
        return new WebParser(new TablaSimbolos()).analizar(lexer);
    }

    private void esperaError(String fuente) throws LexicalException {
        assertThrows(SyntaxException.class, () -> parsear(fuente));
    }

    // ── Casos válidos ────────────────────────────────────────────────────────

    @Test
    void paginaMinima() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Ejemplo {
                variables: nombre
            }
            """);
        assertEquals("Ejemplo", pagina.getNombre());
        assertEquals(List.of("nombre"), pagina.getDeclaracion().getVariables());
        assertTrue(pagina.getElementos().isEmpty());
    }

    @Test
    void variasVariables() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: a, b, c
            }
            """);
        assertEquals(List.of("a", "b", "c"), pagina.getDeclaracion().getVariables());
    }

    @Test
    void componenteSimple() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: x
                pie {
                    texto: "pie de página"
                }
            }
            """);
        assertEquals(1, pagina.getElementos().size());
        NodoComponente pie = (NodoComponente) pagina.getElementos().get(0);
        assertEquals("pie", pie.getTipo());
        assertNull(pie.getId());
    }

    @Test
    void componenteConId() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: x
                seccion intro {
                    titulo: "Hola"
                }
            }
            """);
        NodoComponente seccion = (NodoComponente) pagina.getElementos().get(0);
        assertEquals("seccion", seccion.getTipo());
        assertEquals("intro", seccion.getId());
    }

    @Test
    void propiedadesSeparadasPorComa() throws Exception {
        // Coma como separador opcional entre propiedades dentro de un bloque
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: x
                boton { texto: "Ir", enlace: "#top" }
            }
            """);
        NodoComponente boton = (NodoComponente) pagina.getElementos().get(0);
        assertEquals(2, boton.getHijos().size());
    }

    @Test
    void estiloSimple() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: x
                encabezado {
                    estilo: fondo-oscuro, centrado
                }
            }
            """);
        NodoComponente enc = (NodoComponente) pagina.getElementos().get(0);
        NodoPropiedadEstilo estilo = (NodoPropiedadEstilo) enc.getHijos().get(0);
        assertEquals(List.of("fondo-oscuro", "centrado"), estilo.getEstilos());
    }

    @Test
    void estiloColumnas() throws Exception {
        // "columnas" es ambiguo: se tokeniza como COLUMNAS_COMP pero es estilo válido
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: x
                lista catalogo {
                    estilo: columnas
                }
            }
            """);
        NodoComponente lista = (NodoComponente) pagina.getElementos().get(0);
        NodoPropiedadEstilo estilo = (NodoPropiedadEstilo) lista.getHijos().get(0);
        assertEquals(List.of("columnas"), estilo.getEstilos());
    }

    @Test
    void componenteColumnas() throws Exception {
        // "columnas" también como tipo de componente
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: x
                columnas {
                    texto: "celda"
                }
            }
            """);
        NodoComponente col = (NodoComponente) pagina.getElementos().get(0);
        assertEquals("columnas", col.getTipo());
    }

    @Test
    void repite() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: items
                lista {
                    repite ( items )
                        bloque { texto: "item" }
                    fin-repite
                }
            }
            """);
        NodoComponente lista = (NodoComponente) pagina.getElementos().get(0);
        NodoRepite repite = (NodoRepite) lista.getHijos().get(0);
        assertEquals("items", repite.getVariable());
        assertEquals(1, repite.getCuerpo().size());
    }

    @Test
    void siSinoFinSi() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: stock
                seccion {
                    si ( stock == 0 ) entonces
                        texto: "Sin stock"
                    sino
                        texto: "Hay stock"
                    fin-si
                }
            }
            """);
        NodoComponente seccion = (NodoComponente) pagina.getElementos().get(0);
        NodoSi si = (NodoSi) seccion.getHijos().get(0);
        assertEquals(1, si.getRamaEntonces().size());
        assertEquals(1, si.getRamaSino().size());
    }

    @Test
    void siConRamaSinoVacia() throws Exception {
        // rama sino puede estar vacía pero debe aparecer
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: flag
                seccion {
                    si ( flag == "si" ) entonces
                        texto: "activo"
                    sino
                    fin-si
                }
            }
            """);
        NodoComponente seccion = (NodoComponente) pagina.getElementos().get(0);
        NodoSi si = (NodoSi) seccion.getHijos().get(0);
        assertTrue(si.getRamaSino().isEmpty());
    }

    @Test
    void comparacionConNumero() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: cantidad
                seccion {
                    si ( cantidad > 10 ) entonces
                        texto: "muchos"
                    sino
                        texto: "pocos"
                    fin-si
                }
            }
            """);
        NodoComponente seccion = (NodoComponente) pagina.getElementos().get(0);
        NodoSi si = (NodoSi) seccion.getHijos().get(0);
        NodoComparacion comp = si.getComparacion();
        assertEquals(">", comp.getOperadorRelacional());
        assertEquals("cantidad", comp.getIzquierdo().getValor());
        assertEquals("10", comp.getDerecho().getValor());
    }

    @Test
    void componentesAnidados() throws Exception {
        NodoPagina pagina = parsear("""
            pagina Demo {
                variables: x
                encabezado {
                    nav {
                        boton { texto: "Inicio" }
                    }
                }
            }
            """);
        NodoComponente enc = (NodoComponente) pagina.getElementos().get(0);
        NodoComponente nav = (NodoComponente) enc.getHijos().get(0);
        NodoComponente boton = (NodoComponente) nav.getHijos().get(0);
        assertEquals("boton", boton.getTipo());
    }

    @Test
    void inputCompletoAutoStock() throws Exception {
        // El ejemplo completo del plan debe parsear sin errores
        String fuente = """
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
                    texto: "Descripcion del sistema"
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
            """;
        NodoPagina pagina = parsear(fuente);
        assertEquals("AutoStock", pagina.getNombre());
        assertEquals(List.of("productos", "usuario"), pagina.getDeclaracion().getVariables());
        assertEquals(4, pagina.getElementos().size()); // encabezado, seccion inicio, seccion inv, pie
    }

    // ── Casos de error ───────────────────────────────────────────────────────

    @Test
    void errorVariableNoDeclarada() throws LexicalException {
        esperaError("""
            pagina Demo {
                variables: nombre
                seccion {
                    texto: apellido
                }
            }
            """);
    }

    @Test
    void errorVariableDuplicada() throws LexicalException {
        esperaError("""
            pagina Demo {
                variables: nombre, nombre
            }
            """);
    }

    @Test
    void errorIdComponenteDuplicado() throws LexicalException {
        esperaError("""
            pagina Demo {
                variables: x
                seccion intro { texto: "a" }
                seccion intro { texto: "b" }
            }
            """);
    }

    @Test
    void errorRepiteVariableNoDeclarada() throws LexicalException {
        esperaError("""
            pagina Demo {
                variables: x
                lista {
                    repite ( items )
                        bloque { texto: "item" }
                    fin-repite
                }
            }
            """);
    }

    @Test
    void errorFaltaLlaveInicial() throws LexicalException {
        esperaError("pagina Demo variables: x }");
    }

    @Test
    void errorFaltaFinSi() throws LexicalException {
        esperaError("""
            pagina Demo {
                variables: flag
                seccion {
                    si ( flag == "x" ) entonces
                        texto: "ok"
                    sino
                        texto: "no"
                }
            }
            """);
    }

    @Test
    void errorFaltaFinRepite() throws LexicalException {
        esperaError("""
            pagina Demo {
                variables: items
                lista {
                    repite ( items )
                        bloque { texto: "x" }
                }
            }
            """);
    }

    @Test
    void errorFaltaSinoEnCondicional() throws LexicalException {
        esperaError("""
            pagina Demo {
                variables: flag
                seccion {
                    si ( flag == "x" ) entonces
                        texto: "ok"
                    fin-si
                }
            }
            """);
    }
}
