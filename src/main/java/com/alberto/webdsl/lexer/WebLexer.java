package com.alberto.webdsl.lexer;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alberto.webdsl.tokens.TipoToken;
import com.alberto.webdsl.tokens.Token;

/**
 * Analizador léxico de WebDSL. Recibe el texto fuente de un archivo {@code .webdsl}
 * y produce una lista de {@link com.alberto.webdsl.tokens.Token} en el orden en que
 * aparecen en la entrada. Los espacios y comentarios ({@code #...}) se descartan.
 * Lanza {@link LexicalException} si encuentra una secuencia no reconocida.
 */
public class WebLexer {
    private final ArrayList<TipoToken> tipos = new ArrayList<>();
    private final ArrayList<Token> tokens = new ArrayList<>();

    public WebLexer() {
        // 1. Literales
        tipos.add(new TipoToken(TipoToken.NUMERO,  "[0-9]+(\\.[0-9]+)?"));
        tipos.add(new TipoToken(TipoToken.CADENA,  "\"[^\"]*\""));

        // 2. Operadores relacionales (compuestos antes que simples)
        tipos.add(new TipoToken(TipoToken.OPRELACIONAL, "<=|>=|==|!=|<|>"));

        // 3. Símbolos
        tipos.add(new TipoToken(TipoToken.LLAVEIZQ,      "\\{"));
        tipos.add(new TipoToken(TipoToken.LLAVEDER,      "\\}"));
        tipos.add(new TipoToken(TipoToken.PARENTESISIZQ, "\\("));
        tipos.add(new TipoToken(TipoToken.PARENTESISDER, "\\)"));
        tipos.add(new TipoToken(TipoToken.DOSPUNTOS,     ":"));
        tipos.add(new TipoToken(TipoToken.COMA,          ","));

        // 4. Palabras reservadas multi-palabra primero (contienen guión)
        tipos.add(new TipoToken(TipoToken.FINREPITE, "fin-repite"));
        tipos.add(new TipoToken(TipoToken.FINSI,     "fin-si"));

        // 5. Palabras reservadas — sino ANTES que si (ambas matchean en la misma posición)
        tipos.add(new TipoToken(TipoToken.PAGINA,       "pagina"));
        tipos.add(new TipoToken(TipoToken.VARIABLES,    "variables"));
        tipos.add(new TipoToken(TipoToken.ESTILO,       "estilo"));
        tipos.add(new TipoToken(TipoToken.ENCABEZADO,   "encabezado"));
        tipos.add(new TipoToken(TipoToken.NAV,          "nav"));
        tipos.add(new TipoToken(TipoToken.SECCION,      "seccion"));
        tipos.add(new TipoToken(TipoToken.LISTA,        "lista"));
        tipos.add(new TipoToken(TipoToken.TARJETA,      "tarjeta"));
        tipos.add(new TipoToken(TipoToken.COLUMNAS_COMP,"columnas"));
        tipos.add(new TipoToken(TipoToken.BOTON,        "boton"));
        tipos.add(new TipoToken(TipoToken.PIE,          "pie"));
        tipos.add(new TipoToken(TipoToken.BLOQUE,       "bloque"));
        tipos.add(new TipoToken(TipoToken.SUBTITULO,    "subtitulo"));
        tipos.add(new TipoToken(TipoToken.TITULO,       "titulo"));
        tipos.add(new TipoToken(TipoToken.TEXTO,        "texto"));
        tipos.add(new TipoToken(TipoToken.IMAGEN,       "imagen"));
        tipos.add(new TipoToken(TipoToken.ENLACE,       "enlace"));
        tipos.add(new TipoToken(TipoToken.ICONO,        "icono"));
        tipos.add(new TipoToken(TipoToken.ENTONCES,     "entonces"));
        tipos.add(new TipoToken(TipoToken.SINO,         "sino"));
        tipos.add(new TipoToken(TipoToken.SI,           "si"));
        tipos.add(new TipoToken(TipoToken.REPITE,       "repite"));

        // 6. ID_ESTILO antes que ID — relleno-grande antes que relleno
        tipos.add(new TipoToken(TipoToken.ID_ESTILO,
                "centrado|izquierda|derecha|" +
                "fondo-oscuro|fondo-claro|fondo-primario|" +
                "sombra|redondeado|relleno-grande|relleno|" +
                "negrita|grande|chico|columnas"));

        // 7. Identificador genérico (captura lo no reservado)
        tipos.add(new TipoToken(TipoToken.ID, "[a-zA-Z_][a-zA-Z0-9_]*"));

        // 8. Ignorados y error
        tipos.add(new TipoToken(TipoToken.ESPACIO,    "[ \\t\\f\\r\\n]+"));
        tipos.add(new TipoToken(TipoToken.COMENTARIO, "#[^\\n]*"));
        tipos.add(new TipoToken(TipoToken.ERROR,      "[^ \\t\\f\\r\\n]+"));
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void analizar(String entrada) throws LexicalException {
        StringBuilder er = new StringBuilder();

        for (TipoToken tt : tipos) {
            er.append(String.format("|(?<%s>%s)", grupo(tt.getNombre()), tt.getPatron()));
        }

        Pattern p = Pattern.compile(er.substring(1));
        Matcher m = p.matcher(entrada);

        while (m.find()) {
            for (TipoToken tt : tipos) {
                if (m.group(grupo(TipoToken.ESPACIO)) != null || m.group(grupo(TipoToken.COMENTARIO)) != null)
                    break;
                else if (m.group(grupo(tt.getNombre())) != null) {
                    if (tt.getNombre().equals(TipoToken.ERROR))
                        throw new LexicalException(m.group(grupo(tt.getNombre())));

                    String nombre = m.group(grupo(tt.getNombre()));

                    if (tt.getNombre().equals(TipoToken.CADENA))
                        nombre = nombre.substring(1, nombre.length() - 1);

                    tokens.add(new Token(tt, nombre));
                    break;
                }
            }
        }
    }

    private String grupo(String nombre) {
        return nombre.replace("_", "");
    }
}
