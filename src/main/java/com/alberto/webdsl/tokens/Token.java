package com.alberto.webdsl.tokens;

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
