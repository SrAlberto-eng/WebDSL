package com.web.traductor.astnodes;

import java.util.List;

import com.web.traductor.patronvisitor.ASTVisitor;

/**
 * Representa un componente Markup con cuerpo anidado:
 *   encabezado { ... }
 *   seccion miId { ... }
 *
 * El campo {@code id} es opcional (null si el componente no tiene identificador).
 * El campo {@code tipo} contiene el lexema del componente ("encabezado", "nav", etc.).
 */
public class NodoComponente extends NodoElemento {
    private final String tipo;
    private final String id;
    private final List<NodoElemento> hijos;

    public NodoComponente(String tipo, String id, List<NodoElemento> hijos) {
        this.tipo = tipo;
        this.id = id;
        this.hijos = hijos;
    }

    public String getTipo() { return tipo; }
    public String getId() { return id; }
    public List<NodoElemento> getHijos() { return hijos; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
