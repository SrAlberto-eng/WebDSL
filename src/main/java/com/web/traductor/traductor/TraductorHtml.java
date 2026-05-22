package com.web.traductor.traductor;

import com.web.traductor.astnodes.*;
import com.web.traductor.astnodes.NodoValor.TipoValor;
import com.web.traductor.patronvisitor.ASTVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generador de HTML para WebDSL. Implementa {@link ASTVisitor} y recorre el AST
 * producido por {@link com.web.traductor.parser.WebParser} para emitir un único
 * archivo {@code .html} autocontenido con CSS y JavaScript embebidos.
 *
 * <p>Uso:
 * <pre>
 *     String html = new TraductorHtml().generar(nodoPagina);
 * </pre>
 *
 * @throws GenerationException si el AST contiene construcciones semánticamente
 *         inválidas para la generación (p. ej. {@code enlace} fuera de {@code boton})
 */
public class TraductorHtml implements ASTVisitor {

    private StringBuilder code;
    private int nivel = 0;
    private Set<String> variablesArray = new HashSet<>();

    // ── API pública ──────────────────────────────────────────────────────────

    public String generar(NodoPagina pagina) {
        variablesArray = escanearVariablesArray(pagina);
        code = new StringBuilder();
        pagina.accept(this);
        return code.toString();
    }

    // ── Visitor ──────────────────────────────────────────────────────────────

    @Override
    public void visit(NodoPagina nodo) {
        code.append("<!DOCTYPE html>\n");
        code.append("<html lang=\"es\">\n");
        code.append("<head>\n");
        code.append("  <meta charset=\"UTF-8\">\n");
        code.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        code.append("  <title>").append(nodo.getNombre()).append("</title>\n");
        code.append("  <style>\n");
        code.append(CSS);
        code.append("  </style>\n");
        code.append("</head>\n");
        code.append("<body>\n");

        nivel = 1;
        nodo.getDeclaracion().accept(this);
        code.append("\n");

        for (NodoElemento el : nodo.getElementos()) {
            el.accept(this);
        }

        code.append("\n");
        ind(); code.append("<script>\n");
        ind(); code.append("  document.querySelectorAll('[data-var]').forEach(function(el) {\n");
        ind(); code.append("    var v = window[el.getAttribute('data-var')];\n");
        ind(); code.append("    el.textContent = (v !== undefined && v !== null) ? v : \"\";\n");
        ind(); code.append("  });\n");
        ind(); code.append("</script>\n");

        code.append("</body>\n");
        code.append("</html>\n");
    }

    @Override
    public void visit(NodoDeclaracion nodo) {
        ind(); code.append("<script>\n");
        ind(); code.append("  // ============================================\n");
        ind(); code.append("  //  DATOS DE LA PÁGINA — editar antes de servir\n");
        ind(); code.append("  // ============================================\n");
        for (String var : nodo.getVariables()) {
            ind();
            if (variablesArray.contains(var)) {
                code.append("  var ").append(var).append(" = [];\n");
            } else {
                code.append("  var ").append(var).append(" = \"\";\n");
            }
        }
        ind(); code.append("</script>\n");
    }

    @Override
    public void visit(NodoComponente nodo) {
        // Primer pase: recolectar estilos y propiedades especiales
        List<String> clases = new ArrayList<>();
        String enlace = null;

        for (NodoElemento hijo : nodo.getHijos()) {
            if (hijo instanceof NodoPropiedadEstilo ps) {
                clases.addAll(ps.getEstilos());
            }
            if (hijo instanceof NodoPropiedadTexto pt && pt.getNombre().equals("enlace")) {
                if (!nodo.getTipo().equals("boton")) {
                    throw new GenerationException(
                        "La propiedad 'enlace' solo es válida dentro de un 'boton'. " +
                        "Se encontró dentro de '" + nodo.getTipo() + "'."
                    );
                }
                enlace = pt.getValor().getValor();
            }
        }

        // Clases implícitas según tipo
        if (nodo.getTipo().equals("tarjeta"))  clases.add(0, "tarjeta");
        if (nodo.getTipo().equals("columnas")) clases.add(0, "columnas");

        String tag = mapearTag(nodo.getTipo());

        ind(); code.append("<").append(tag);
        if (nodo.getId()   != null) code.append(" id=\"").append(nodo.getId()).append("\"");
        if (!clases.isEmpty())      code.append(" class=\"").append(String.join(" ", clases)).append("\"");
        if (enlace != null)         code.append(" onclick=\"location.href='").append(enlace).append("'\"");
        code.append(">\n");

        // Segundo pase: visitar hijos de contenido (omitir estilo y enlace)
        nivel++;
        for (NodoElemento hijo : nodo.getHijos()) {
            if (hijo instanceof NodoPropiedadEstilo) continue;
            if (hijo instanceof NodoPropiedadTexto pt && pt.getNombre().equals("enlace")) continue;
            hijo.accept(this);
        }
        nivel--;

        ind(); code.append("</").append(tag).append(">\n");
    }

    @Override
    public void visit(NodoPropiedadTexto nodo) {
        String valor = nodo.getValor().getValor();
        boolean esVar = nodo.getValor().getTipo() == TipoValor.ID;

        ind();
        switch (nodo.getNombre()) {
            case "titulo" -> {
                if (esVar) code.append("<h1 data-var=\"").append(valor).append("\"></h1>\n");
                else       code.append("<h1>").append(valor).append("</h1>\n");
            }
            case "subtitulo" -> {
                if (esVar) code.append("<h2 data-var=\"").append(valor).append("\"></h2>\n");
                else       code.append("<h2>").append(valor).append("</h2>\n");
            }
            case "texto" -> {
                if (esVar) code.append("<p data-var=\"").append(valor).append("\"></p>\n");
                else       code.append("<p>").append(valor).append("</p>\n");
            }
            case "imagen" ->
                code.append("<img src=\"").append(valor).append("\" alt=\"\">\n");
            case "icono" -> {
                if (esVar) code.append("<span class=\"icono\" data-var=\"").append(valor).append("\"></span>\n");
                else       code.append("<span class=\"icono\">").append(valor).append("</span>\n");
            }
            // enlace se maneja en visit(NodoComponente); aquí no se emite nada
        }
    }

    @Override
    public void visit(NodoPropiedadEstilo nodo) {
        // Los estilos se recolectan en visit(NodoComponente); no se emite nada aquí
    }

    @Override
    public void visit(NodoSi nodo) {
        ind(); code.append("<script>\n");
        nivel++;
        ind(); code.append("if (");
        nodo.getComparacion().accept(this);
        code.append(") {\n");
        emitirComoDocumentWrite(nodo.getRamaEntonces());
        ind(); code.append("} else {\n");
        emitirComoDocumentWrite(nodo.getRamaSino());
        ind(); code.append("}\n");
        nivel--;
        ind(); code.append("</script>\n");
    }

    @Override
    public void visit(NodoRepite nodo) {
        ind(); code.append("<script>\n");
        nivel++;
        ind(); code.append("(").append(nodo.getVariable()).append(" || []).forEach(function(item) {\n");
        emitirComoDocumentWrite(nodo.getCuerpo());
        ind(); code.append("});\n");
        nivel--;
        ind(); code.append("</script>\n");
    }

    @Override
    public void visit(NodoComparacion nodo) {
        nodo.getIzquierdo().accept(this);
        code.append(" ").append(nodo.getOperadorRelacional()).append(" ");
        nodo.getDerecho().accept(this);
    }

    @Override
    public void visit(NodoValor nodo) {
        switch (nodo.getTipo()) {
            case CADENA -> code.append("\"").append(nodo.getValor()).append("\"");
            case NUMERO -> code.append(nodo.getValor());
            case ID     -> code.append(nodo.getValor());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Genera el HTML de una lista de elementos usando un buffer temporal y
     * envuelve cada línea resultante en una llamada {@code document.write(...)}.
     * Se usa para las ramas de {@code si/sino} y el cuerpo de {@code repite}.
     */
    private void emitirComoDocumentWrite(List<NodoElemento> elementos) {
        StringBuilder bufferAnterior = code;
        int nivelAnterior = nivel;

        code = new StringBuilder();
        nivel = 0;
        for (NodoElemento el : elementos) {
            el.accept(this);
        }
        String contenido = code.toString();

        code = bufferAnterior;
        nivel = nivelAnterior;

        for (String linea : contenido.split("\n", -1)) {
            if (!linea.isEmpty()) {
                ind();
                code.append("  document.write('")
                    .append(linea.replace("\\", "\\\\").replace("'", "\\'"))
                    .append("');\n");
            }
        }
    }

    private String mapearTag(String tipo) {
        return switch (tipo) {
            case "encabezado" -> "header";
            case "nav"        -> "nav";
            case "seccion"    -> "section";
            case "lista"      -> "ul";
            case "tarjeta"    -> "li";
            case "columnas"   -> "div";
            case "boton"      -> "button";
            case "pie"        -> "footer";
            case "bloque"     -> "div";
            default -> throw new IllegalArgumentException("Tipo desconocido: " + tipo);
        };
    }

    private void ind() {
        code.append("  ".repeat(nivel));
    }

    // ── CSS fijo ─────────────────────────────────────────────────────────────

    private static final String CSS =
        "    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
        "    html { scroll-behavior: smooth; }\n" +
        "\n" +
        "    body {\n" +
        "      font-family: system-ui, sans-serif;\n" +
        "      font-size: 16px;\n" +
        "      line-height: 1.6;\n" +
        "      color: #1a1a1a;\n" +
        "      background: #ffffff;\n" +
        "    }\n" +
        "\n" +
        "    h1 { font-size: 2rem;   font-weight: 700; line-height: 1.2; }\n" +
        "    h2 { font-size: 1.4rem; font-weight: 600; line-height: 1.3; }\n" +
        "    p  { font-size: 1rem;   color: inherit; }\n" +
        "    img { max-width: 100%; display: block; }\n" +
        "\n" +
        "    header  { display: flex; align-items: center; justify-content: space-between; padding: 0 1.5rem; min-height: 60px; }\n" +
        "    nav     { display: flex; align-items: center; gap: 0.5rem; }\n" +
        "    section { display: block; width: 100%; }\n" +
        "    footer  { display: block; padding: 1.5rem; }\n" +
        "    ul      { list-style: none; padding: 0; }\n" +
        "\n" +
        "    li.tarjeta {\n" +
        "      display: flex; flex-direction: column;\n" +
        "      background: #ffffff; border: 1px solid #e2e8f0;\n" +
        "      overflow: hidden;\n" +
        "    }\n" +
        "    li.tarjeta h2 { padding: 1rem 1rem 0.25rem; }\n" +
        "    li.tarjeta p  { padding: 0 1rem 1rem; color: #555; font-size: 0.9rem; }\n" +
        "    li.tarjeta button { margin: 0 1rem 1rem; align-self: flex-start; }\n" +
        "\n" +
        "    button {\n" +
        "      font-family: inherit; font-size: 0.9rem; font-weight: 500;\n" +
        "      padding: 0.5rem 1.2rem;\n" +
        "      border: 1.5px solid #1a1a1a; background: transparent; color: #1a1a1a;\n" +
        "      cursor: pointer;\n" +
        "    }\n" +
        "    button:hover { background: #1a1a1a; color: #fff; }\n" +
        "    nav button { border: none; background: transparent; color: inherit; padding: 0.4rem 0.9rem; }\n" +
        "    nav button:hover { background: rgba(255,255,255,0.12); color: inherit; }\n" +
        "\n" +
        "    .centrado  { text-align: center; }\n" +
        "    .izquierda { text-align: left;   }\n" +
        "    .derecha   { text-align: right;  }\n" +
        "\n" +
        "    .fondo-oscuro   { background: #1a1a2e; color: #f0f0f0; }\n" +
        "    .fondo-claro    { background: #f5f7fa; color: #1a1a1a; }\n" +
        "    .fondo-primario { background: #1d4ed8; color: #ffffff; }\n" +
        "\n" +
        "    .sombra     { box-shadow: 0 4px 16px rgba(0,0,0,0.10); }\n" +
        "    .redondeado { border-radius: 10px; }\n" +
        "\n" +
        "    .relleno        { padding: 2rem 1.5rem; }\n" +
        "    .relleno-grande { padding: 4rem 1.5rem; }\n" +
        "\n" +
        "    .negrita { font-weight: 700; }\n" +
        "    .grande  { font-size: 1.2rem; }\n" +
        "    .chico   { font-size: 0.8rem; }\n" +
        "\n" +
        "    .columnas {\n" +
        "      display: grid;\n" +
        "      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));\n" +
        "      gap: 1.25rem;\n" +
        "    }\n" +
        "\n" +
        "    @media (max-width: 768px) {\n" +
        "      header { flex-direction: column; align-items: flex-start; gap: 0.5rem; padding: 1rem; }\n" +
        "      nav { flex-wrap: wrap; }\n" +
        "    }\n";

    // Escanea el AST buscando nodos NodoRepite para inferir qué variables son arrays
    private Set<String> escanearVariablesArray(NodoPagina pagina) {
        Set<String> resultado = new HashSet<>();
        escanearElementos(pagina.getElementos(), resultado);
        return resultado;
    }

    private void escanearElementos(List<NodoElemento> elementos, Set<String> resultado) {
        for (NodoElemento el : elementos) {
            if (el instanceof NodoRepite r) {
                resultado.add(r.getVariable());
                escanearElementos(r.getCuerpo(), resultado);
            } else if (el instanceof NodoComponente c) {
                escanearElementos(c.getHijos(), resultado);
            } else if (el instanceof NodoSi s) {
                escanearElementos(s.getRamaEntonces(), resultado);
                escanearElementos(s.getRamaSino(), resultado);
            }
        }
    }
}
