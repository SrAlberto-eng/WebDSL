package com.alberto.webdsl.astnodes;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

public class NodoValor extends Nodo {
    private final String valor;
    private final boolean esVariable;

    public NodoValor(boolean esVariable, String valor) {
        this.esVariable = esVariable;
        this.valor = valor;
    }

    public String getValor() { return valor; }
    public boolean isEsVariable() { return esVariable; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
