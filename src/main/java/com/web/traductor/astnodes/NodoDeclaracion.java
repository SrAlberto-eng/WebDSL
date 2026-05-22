package com.web.traductor.astnodes;

import java.util.List;

import com.web.traductor.patronvisitor.ASTVisitor;

/**
 * Representa la sección de declaración de variables:
 *   variables: nombre, edad, descripcion
 *
 * Cada elemento de {@code variables} es un identificador declarado
 * que puede usarse como valor en propiedades y expresiones de control de flujo.
 */
public class NodoDeclaracion extends NodoElemento {
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
