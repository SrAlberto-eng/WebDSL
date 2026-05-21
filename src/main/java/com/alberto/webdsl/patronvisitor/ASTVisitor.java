package com.alberto.webdsl.patronvisitor;

import com.alberto.webdsl.astnodes.*;

/**
 * Interfaz del patrón Visitor para el AST de WebDSL.
 * Cada implementación (TraductorHtml, etc.) recorre el árbol
 * y genera la salida correspondiente sin modificar los nodos.
 */
public interface ASTVisitor {
    void visit(NodoPagina nodo);
    void visit(NodoDeclaracion nodo);
    void visit(NodoComponente nodo);
    void visit(NodoPropiedadTexto nodo);
    void visit(NodoPropiedadEstilo nodo);
    void visit(NodoSi nodo);
    void visit(NodoRepite nodo);
    void visit(NodoComparacion nodo);
    void visit(NodoValor nodo);
}
