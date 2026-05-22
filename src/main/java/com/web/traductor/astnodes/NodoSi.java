package com.web.traductor.astnodes;

import java.util.List;

import com.web.traductor.patronvisitor.ASTVisitor;

/**
 * Representa una estructura condicional WebDSL:
 *   si (variable == "valor") entonces
 *     <elementos>
 *   sino
 *     <elementos>
 *   fin-si
 *
 * Ambas ramas son obligatorias según la gramática; {@code ramaSino} puede
 * ser una lista vacía pero nunca null.
 */
public class NodoSi extends NodoElemento {
    private final NodoComparacion comparacion;
    private final List<NodoElemento> ramaEntonces;
    private final List<NodoElemento> ramaSino;

    public NodoSi(NodoComparacion comparacion,
                  List<NodoElemento> ramaEntonces,
                  List<NodoElemento> ramaSino) {
        this.comparacion = comparacion;
        this.ramaEntonces = ramaEntonces;
        this.ramaSino = ramaSino;
    }

    public NodoComparacion getComparacion() { return comparacion; }
    public List<NodoElemento> getRamaEntonces() { return ramaEntonces; }
    public List<NodoElemento> getRamaSino() { return ramaSino; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
