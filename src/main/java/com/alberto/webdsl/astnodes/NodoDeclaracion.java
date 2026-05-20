package com.alberto.webdsl.astnodes;

import java.util.List;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

public class NodoDeclaracion extends Nodo {
    private final List<String> variables;

    public NodoDeclaracion(List<String> variables) {
        this.variables = variables;
    }

    public List<String> getVariables() { return variables; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
