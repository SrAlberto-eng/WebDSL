package com.alberto.webdsl.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alberto.webdsl.astnodes.NodoComparacion;
import com.alberto.webdsl.astnodes.NodoComponente;
import com.alberto.webdsl.astnodes.NodoDeclaracion;
import com.alberto.webdsl.astnodes.NodoElemento;
import com.alberto.webdsl.astnodes.NodoPagina;
import com.alberto.webdsl.astnodes.NodoPropiedadEstilo;
import com.alberto.webdsl.astnodes.NodoPropiedadTexto;
import com.alberto.webdsl.astnodes.NodoRepite;
import com.alberto.webdsl.astnodes.NodoSi;
import com.alberto.webdsl.astnodes.NodoValor;
import com.alberto.webdsl.astnodes.NodoValor.TipoValor;
import com.alberto.webdsl.lexer.WebLexer;
import com.alberto.webdsl.simbols.TablaSimbolos;
import com.alberto.webdsl.simbols.Variable;
import com.alberto.webdsl.tokens.TipoToken;
import com.alberto.webdsl.tokens.Token;

/**
 * Parser descendente recursivo para WebDSL.
 * Consume la lista de tokens producida por {@link WebLexer} y construye
 * el AST ({@link NodoPagina}), registrando las variables declaradas
 * en la {@link TablaSimbolos} y validando su uso.
 */
public class WebParser {
    private ArrayList<Token> tokens;
    private int indiceToken = 0;
    private SyntaxException ex;
    private final TablaSimbolos ts;
    private final Set<String> idsComponentes = new HashSet<>();

    public WebParser(TablaSimbolos ts) { this.ts = ts; }

    public NodoPagina analizar(WebLexer lexer) throws SyntaxException {
        tokens = lexer.getTokens();
        NodoPagina pagina = Programa();

        if (pagina != null && indiceToken == tokens.size()) return pagina;

        if (ex == null) ex = new SyntaxException("Error de sintaxis");
        throw ex;
    }

    // ── Producciones gramaticales ────────────────────────────────────────────

    /** pagina ID { Declaracion Secciones } */
    private NodoPagina Programa() {
        if (!match(TipoToken.PAGINA)) return null;

        if (!currentToken(TipoToken.ID)) {
            if (ex == null) ex = new SyntaxException(TipoToken.ID, tokenActual());
            return null;
        }
        String nombre = tokens.get(indiceToken).getNombre();
        match(TipoToken.ID);

        if (!match(TipoToken.LLAVEIZQ)) return null;

        NodoDeclaracion declaracion = Declaracion();
        if (ex != null) return null;

        List<NodoElemento> elementos = Secciones();
        if (ex != null) return null;

        if (!match(TipoToken.LLAVEDER)) return null;

        return new NodoPagina(nombre, declaracion, elementos);
    }

    /** variables : ID , ID , ... */
    private NodoDeclaracion Declaracion() {
        if (!match(TipoToken.VARIABLES)) return null;
        if (!match(TipoToken.DOSPUNTOS)) return null;

        List<String> vars = ListaVariables();
        if (vars == null) return null;

        return new NodoDeclaracion(vars);
    }

    /** ID | ID , ListaVariables */
    private List<String> ListaVariables() {
        List<String> lista = new ArrayList<>();
        do {
            if (!currentToken(TipoToken.ID)) {
                if (ex == null) ex = new SyntaxException(TipoToken.ID, tokenActual());
                return null;
            }
            String nombre = tokens.get(indiceToken).getNombre();
            match(TipoToken.ID);
            try {
                ts.definir(new Variable(nombre, null));
            } catch (SyntaxException e) {
                if (ex == null) ex = e;
                return null;
            }
            lista.add(nombre);
        } while (currentToken(TipoToken.COMA) && match(TipoToken.COMA));

        return lista;
    }

    /** Cero o más Elemento; coma entre elementos es un separador opcional */
    private List<NodoElemento> Secciones() {
        List<NodoElemento> lista = new ArrayList<>();
        NodoElemento el;
        while (ex == null && (el = Elemento()) != null) {
            lista.add(el);
            if (currentToken(TipoToken.COMA)) match(TipoToken.COMA);
        }
        return ex == null ? lista : null;
    }

    /** SI | REPITE | ESTILO | PropiedadTexto | Componente — null si no aplica ninguno */
    private NodoElemento Elemento() {
        if (currentToken(TipoToken.SI))      return Si();
        if (currentToken(TipoToken.REPITE))  return Repite();
        if (currentToken(TipoToken.ESTILO))  return PropiedadEstilo();
        if (esNombrePropiedad())             return PropiedadTexto();
        if (esTipoComponente())              return Componente();
        return null;
    }

    /** TipoComponente [ID] { Secciones } */
    private NodoComponente Componente() {
        String tipo = tokens.get(indiceToken).getNombre();
        indiceToken++;

        String id = null;
        if (currentToken(TipoToken.ID)) {
            id = tokens.get(indiceToken).getNombre();
            match(TipoToken.ID);
            if (idsComponentes.contains(id)) {
                if (ex == null) ex = new SyntaxException("El id '" + id + "' ya esta en uso");
                return null;
            }
            idsComponentes.add(id);
        }

        if (!match(TipoToken.LLAVEIZQ)) return null;
        List<NodoElemento> hijos = Secciones();
        if (ex != null) return null;
        if (!match(TipoToken.LLAVEDER)) return null;

        return new NodoComponente(tipo, id, hijos);
    }

    /** NombrePropiedad : Valor */
    private NodoPropiedadTexto PropiedadTexto() {
        String nombre = tokens.get(indiceToken).getNombre();
        indiceToken++;
        if (!match(TipoToken.DOSPUNTOS)) return null;
        NodoValor valor = Valor();
        if (valor == null) return null;
        return new NodoPropiedadTexto(nombre, valor);
    }

    /** estilo : ID_ESTILO , ID_ESTILO , ... */
    private NodoPropiedadEstilo PropiedadEstilo() {
        if (!match(TipoToken.ESTILO)) return null;
        if (!match(TipoToken.DOSPUNTOS)) return null;
        List<String> estilos = ListaEstilos();
        if (estilos == null) return null;
        return new NodoPropiedadEstilo(estilos);
    }

    /** ID_ESTILO | ID_ESTILO , ListaEstilos — acepta COLUMNAS_COMP porque "columnas" es ambiguo */
    private List<String> ListaEstilos() {
        List<String> lista = new ArrayList<>();
        do {
            if (currentToken(TipoToken.ID_ESTILO) || currentToken(TipoToken.COLUMNAS_COMP)) {
                lista.add(tokens.get(indiceToken).getNombre());
                indiceToken++;
            } else {
                if (ex == null) ex = new SyntaxException(TipoToken.ID_ESTILO, tokenActual());
                return null;
            }
        } while (currentToken(TipoToken.COMA) && match(TipoToken.COMA));
        return lista;
    }

    /** si ( Comparacion ) entonces Secciones sino Secciones fin-si */
    private NodoSi Si() {
        if (!match(TipoToken.SI)) return null;
        if (!match(TipoToken.PARENTESISIZQ)) return null;
        NodoComparacion comp = Comparacion();
        if (comp == null) return null;
        if (!match(TipoToken.PARENTESISDER)) return null;
        if (!match(TipoToken.ENTONCES)) return null;

        List<NodoElemento> entonces = Secciones();
        if (ex != null) return null;
        if (!match(TipoToken.SINO)) return null;

        List<NodoElemento> sino = Secciones();
        if (ex != null) return null;
        if (!match(TipoToken.FINSI)) return null;

        return new NodoSi(comp, entonces, sino);
    }

    /** repite ( ID ) Secciones fin-repite */
    private NodoRepite Repite() {
        if (!match(TipoToken.REPITE)) return null;
        if (!match(TipoToken.PARENTESISIZQ)) return null;

        if (!currentToken(TipoToken.ID)) {
            if (ex == null) ex = new SyntaxException(TipoToken.ID, tokenActual());
            return null;
        }
        String variable = tokens.get(indiceToken).getNombre();
        match(TipoToken.ID);
        try {
            ts.resolver(variable);
        } catch (SyntaxException e) {
            if (ex == null) ex = e;
            return null;
        }

        if (!match(TipoToken.PARENTESISDER)) return null;

        List<NodoElemento> cuerpo = Secciones();
        if (ex != null) return null;
        if (!match(TipoToken.FINREPITE)) return null;

        return new NodoRepite(variable, cuerpo);
    }

    /** Valor OPRELACIONAL Valor */
    private NodoComparacion Comparacion() {
        NodoValor izq = Valor();
        if (izq == null) return null;

        if (!currentToken(TipoToken.OPRELACIONAL)) {
            if (ex == null) ex = new SyntaxException(TipoToken.OPRELACIONAL, tokenActual());
            return null;
        }
        String op = tokens.get(indiceToken).getNombre();
        match(TipoToken.OPRELACIONAL);

        NodoValor der = Valor();
        if (der == null) return null;

        return new NodoComparacion(der, izq, op);
    }

    /** ID | CADENA | NUMERO — valida declaración cuando es ID */
    private NodoValor Valor() {
        if (currentToken(TipoToken.CADENA)) {
            String v = tokens.get(indiceToken).getNombre();
            match(TipoToken.CADENA);
            return new NodoValor(TipoValor.CADENA, v);
        }
        if (currentToken(TipoToken.NUMERO)) {
            String v = tokens.get(indiceToken).getNombre();
            match(TipoToken.NUMERO);
            return new NodoValor(TipoValor.NUMERO, v);
        }
        if (currentToken(TipoToken.ID)) {
            String v = tokens.get(indiceToken).getNombre();
            match(TipoToken.ID);
            try {
                ts.resolver(v);
            } catch (SyntaxException e) {
                if (ex == null) ex = e;
                return null;
            }
            return new NodoValor(TipoValor.ID, v);
        }
        if (ex == null) ex = new SyntaxException("Se esperaba un valor (cadena, numero o variable)");
        return null;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private boolean esTipoComponente() {
        return currentToken(TipoToken.ENCABEZADO)
            || currentToken(TipoToken.NAV)
            || currentToken(TipoToken.SECCION)
            || currentToken(TipoToken.LISTA)
            || currentToken(TipoToken.TARJETA)
            || currentToken(TipoToken.COLUMNAS_COMP)
            || currentToken(TipoToken.BOTON)
            || currentToken(TipoToken.PIE)
            || currentToken(TipoToken.BLOQUE);
    }

    private boolean esNombrePropiedad() {
        return currentToken(TipoToken.TEXTO)
            || currentToken(TipoToken.TITULO)
            || currentToken(TipoToken.SUBTITULO)
            || currentToken(TipoToken.IMAGEN)
            || currentToken(TipoToken.ENLACE)
            || currentToken(TipoToken.ICONO);
    }

    private String tokenActual() {
        return indiceToken < tokens.size()
            ? tokens.get(indiceToken).getTipo().getNombre()
            : "fin de entrada";
    }

    private boolean match(String nombre) {
        if (currentToken(nombre)) {
            indiceToken++;
            return true;
        }
        if (ex == null)
            ex = new SyntaxException(nombre, tokenActual());
        return false;
    }

    private boolean currentToken(String nombre) {
        if (indiceToken >= tokens.size()) return false;
        return tokens.get(indiceToken).getTipo().getNombre().equals(nombre);
    }
}
