package com.alberto.webdsl.astnodes;

import com.alberto.webdsl.patronvisitor.ASTVisitor;

public abstract class Nodo {
    public abstract void accept(ASTVisitor visitor);
}
