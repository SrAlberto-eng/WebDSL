package com.alberto.webdsl.astnodes;

import java.util.List;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

/**
 * Nodo raíz del AST. Representa la construcción completa de una página WebDSL:
 *   pagina MiSitio {
 *     variables: ...
 *     <elementos>
 *   }
 *
 * El parser construye este nodo al reconocer la producción {@code <Programa>}.
 */
public class NodoPagina extends Nodo {
    private final String nombre;
    private final NodoDeclaracion declaracion;
    private final List<NodoElemento> elementos;

    public NodoPagina(String nombre, NodoDeclaracion declaracion, List<NodoElemento> elementos) {
        this.nombre = nombre;
        this.declaracion = declaracion;
        this.elementos = elementos;
    }

    public String getNombre() { return nombre; }
    public NodoDeclaracion getDeclaracion() { return declaracion; }
    public List<NodoElemento> getElementos() { return elementos; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
