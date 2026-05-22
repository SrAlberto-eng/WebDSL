package com.web.traductor.astnodes;

import com.web.traductor.patronvisitor.ASTVisitor;

/**
 * Representa un valor literal o referencia a variable en una expresión Markup.
 * El tipo distingue si el valor es un identificador de variable, una cadena o un número.
 *   titulo: "Hola"    → TipoValor.CADENA
 *   texto: miVar      → TipoValor.ID
 *   si (edad > 18)    → TipoValor.NUMERO
 */
public class NodoValor extends Nodo {

    public enum TipoValor { ID, CADENA, NUMERO }

    private final TipoValor tipo;
    private final String valor;

    public NodoValor(TipoValor tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public TipoValor getTipo() { return tipo; }
    public String getValor() { return valor; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
