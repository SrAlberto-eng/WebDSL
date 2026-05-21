package com.alberto.webdsl.astnodes;

import java.util.List;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

/**
 * Representa la propiedad de estilos CSS:
 *   estilo: centrado, fondo-oscuro, sombra
 *
 * Cada elemento de {@code estilos} es un ID_ESTILO válido que se mapeará
 * directamente como clase CSS en el HTML generado.
 */
public class NodoPropiedadEstilo extends NodoElemento {
    private final List<String> estilos;

    public NodoPropiedadEstilo(List<String> estilos) {
        this.estilos = estilos;
    }

    public List<String> getEstilos() { return estilos; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
