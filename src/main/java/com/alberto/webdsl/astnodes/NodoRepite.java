package com.alberto.webdsl.astnodes;

import java.util.List;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

public class NodoRepite extends Nodo {
    private final String variable;
    private final NodoValor valorInicial;
    private final NodoValor valorFinal;
    private final List<Nodo> cuerpo;

    public NodoRepite(NodoValor valorFinal, NodoValor valorInicial, String variable, List<Nodo> cuerpo) {
        this.valorFinal = valorFinal;
        this.valorInicial = valorInicial;
        this.variable = variable;
        this.cuerpo = cuerpo;
    }

    public NodoValor getValorInicial() { return valorInicial; }
    public NodoValor getValorFinal() { return valorFinal; }
    public String getVariable() { return variable; }
    public List<Nodo> getCuerpo() { return cuerpo; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
