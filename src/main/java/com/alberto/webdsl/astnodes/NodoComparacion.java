package com.alberto.webdsl.astnodes;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

/**
 * Representa una expresión relacional usada en condiciones:
 *   variable == "valor"
 *   edad > 18
 *
 * Los operadores válidos son: ==, !=, <, >, <=, >=
 */
public class NodoComparacion extends Nodo {
    private final NodoValor izquierdo;
    private final String operadorRelacional;
    private final NodoValor derecho;

    public NodoComparacion(NodoValor derecho, NodoValor izquierdo, String operadorRelacional) {
        this.derecho = derecho;
        this.izquierdo = izquierdo;
        this.operadorRelacional = operadorRelacional;
    }

    public NodoValor getIzquierdo() { return izquierdo; }
    public String getOperadorRelacional() { return operadorRelacional; }
    public NodoValor getDerecho() { return derecho; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
