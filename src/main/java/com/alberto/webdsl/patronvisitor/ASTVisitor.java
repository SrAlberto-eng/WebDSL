package com.alberto.webdsl.patronvisitor;

import com.alberto.webdsl.astnodes.*;

public interface ASTVisitor {
    void visit(NodoComparacion nodo);
    void visit(NodoDeclaracion nodo);
    void visit(NodoRepite nodo);
    void visit(NodoSi nodo);
    void visit(NodoValor nodo);
}
