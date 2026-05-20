package com.alberto.webdsl.simbols;

public class Simbolo {
    private final String nombre;
    private Tipo tipo;

    public Simbolo(String nombre, Tipo tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Simbolo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return nombre + " : " + tipo;
    }
}
