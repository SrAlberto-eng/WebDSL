package com.web.traductor.tokens;

/**
 * Define el nombre y el patrón regex de un tipo de token Markup.
 * Las constantes estáticas identifican cada categoría léxica del lenguaje:
 * palabras reservadas, símbolos, literales e identificadores.
 */
public class TipoToken {
    private final String nombre;
    private final String patron;

    public TipoToken(String nombre, String patron) {
        this.nombre = nombre;
        this.patron = patron;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPatron() {
        return patron;
    }


    /// Palabras reservadas (componentes y estuctura)
    public static String PAGINA = "PAGINA";
    public static String VARIABLES = "VARIABLES";
    public static String ESTILO = "ESTILO";
    public static String ENCABEZADO = "ENCABEZADO";
    public static String NAV = "NAV";
    public static String SECCION = "SECCION";
    public static String LISTA = "LISTA";
    public static String TARJETA = "TARJETA";
    public static String COLUMNAS_COMP = "COLUMNAS_COMP";
    public static String BOTON = "BOTON";
    public static String PIE = "PIE";
    public static String BLOQUE = "BLOQUE";

    /// Palabras reservadas (propeidades)
    public static String TEXTO = "TEXTO";
    public static String TITULO = "TITULO";
    public static String SUBTITULO = "SUBTITULO";
    public static String IMAGEN = "IMAGEN";
    public static String ENLACE = "ENLACE";
    public static String ICONO = "ICONO";

    /// Palabras de control de flujo ( scripts embebidos )
    public static String SI = "SI";
    public static String ENTONCES = "ENTONCES";
    public static String SINO = "SINO";
    public static String FINSI = "FINSI";
    public static String REPITE = "REPITE";
    public static String FINREPITE = "FINREPITE";

    /// Simbolos 
    public static String LLAVEIZQ = "LLAVEIZQ";
    public static String LLAVEDER = "LLAVEDER";
    public static String PARENTESISIZQ = "PARENTESISIZQ";
    public static String PARENTESISDER = "PARENTESISDER";
    public static String DOSPUNTOS = "DOSPUNTOS";
    public static String COMA = "COMA";
    public static String OPRELACIONAL = "OPRELACIONAL";

    /// Tokens literales e identificadores
    public static String NUMERO = "NUMERO";
    public static String CADENA = "CADENA";
    public static String ID = "ID";
    public static String ID_ESTILO = "ID_ESTILO";

    /// Tokens que son ignorados
    public static String ESPACIO = "ESPACIO";
    public static String COMENTARIO = "COMENTARIO";
    public static String ERROR = "ERROR";
}
