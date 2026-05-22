package com.web.traductor.simbols;

/**
 * Símbolo que representa una variable declarada en la sección
 * {@code variables:} de una página Markup.
 */
public class Variable extends Simbolo {
    public Variable(String nombre, Tipo tipo) {
        super(nombre, tipo);
    }
}
