package com.web.traductor.astnodes;

import com.web.traductor.patronvisitor.ASTVisitor;

/**
 * Clase base de todos los nodos del árbol de sintaxis abstracta (AST).
 * Declara el método {@code accept} que permite a cualquier visitante
 * recorrer el árbol sin modificar la estructura de los nodos.
 */
public abstract class Nodo {
    public abstract void accept(ASTVisitor visitor);
}
