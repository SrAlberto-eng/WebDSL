package com.alberto.webdsl.astnodes;

import java.util.List;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

public class NodoSi extends Nodo {
    private final NodoComparacion comparacion;
    private final List<Nodo> enunciados;

    public NodoSi(NodoComparacion comparacion, List<Nodo> enunciados) {
        this.comparacion = comparacion;
        this.enunciados = enunciados;
    }

    public NodoComparacion getComparacion() { return comparacion; }
    public List<Nodo> getEnunciados() { return enunciados; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
