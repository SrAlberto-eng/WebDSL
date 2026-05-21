package com.alberto.webdsl.tokens;

/**
 * Unidad léxica producida por el analizador léxico.
 * Agrupa el tipo de token ({@link TipoToken}) y el lexema reconocido en la entrada.
 */
public class Token {
    private final TipoToken tipo;
    private final String nombre;

    public Token(TipoToken tipo, String nombre) {
        this.tipo = tipo;
        this.nombre = nombre;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return String.format("<%s, \"%s\">", tipo.getNombre(), nombre);
    }
}
