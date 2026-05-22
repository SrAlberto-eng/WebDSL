package com.web.traductor.lexer;

/**
 * Excepción lanzada cuando el analizador léxico encuentra
 * una secuencia de caracteres que no corresponde a ningún token válido.
 */
public class LexicalException extends Exception {
    public LexicalException(String message) {
        super("El token '" + message + "' es invalido");
    }
}
