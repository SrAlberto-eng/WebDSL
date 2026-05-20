package com.alberto.webdsl.parser;

public class SyntaxException extends Exception {
    public SyntaxException(String message) {
        super(message);
    }

    public SyntaxException(String ms1, String ms2) {
        super("Se esperaba un token '" + ms1 + "' y se encontró '" + ms2 + "'");
    }
}
