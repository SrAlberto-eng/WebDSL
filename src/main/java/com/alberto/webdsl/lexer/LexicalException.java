package com.alberto.webdsl.lexer;

public class LexicalException extends Exception {
    public LexicalException(String message) {
        super("El token '" + message + "' es invalido");
    }
}
