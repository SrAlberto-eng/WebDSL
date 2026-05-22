package com.web.traductor.astnodes;

import java.util.List;

import com.web.traductor.patronvisitor.ASTVisitor;

/**
 * Nodo del AST para el bloque repite/fin-repite: itera sobre un array y repite el cuerpo una vez por elemento:
 *   repite (items)
 *     <elementos>
 *   fin-repite
 *
 * Itera sobre la variable declarada en la sección {@code variables:}.
 */
public class NodoRepite extends NodoElemento {
    private final String variable;
    private final List<NodoElemento> cuerpo;

    public NodoRepite(String variable, List<NodoElemento> cuerpo) {
        this.variable = variable;
        this.cuerpo = cuerpo;
    }

    public String getVariable() { return variable; }
    public List<NodoElemento> getCuerpo() { return cuerpo; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
