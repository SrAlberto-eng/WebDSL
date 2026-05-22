package com.web.traductor.astnodes;

import com.web.traductor.patronvisitor.ASTVisitor;

/**
 * Representa una propiedad de contenido textual:
 *   titulo: "Hola"
 *   texto: miVariable
 *   imagen: "url"
 *
 * El campo {@code nombre} contiene el tipo de propiedad ("titulo", "texto", etc.).
 * El campo {@code valor} puede ser una cadena literal, un número o un identificador de variable.
 */
public class NodoPropiedadTexto extends NodoElemento {
    private final String nombre;
    private final NodoValor valor;

    public NodoPropiedadTexto(String nombre, NodoValor valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getNombre() { return nombre; }
    public NodoValor getValor() { return valor; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
