package com.alberto.webdsl.traductor;

/**
 * Excepción lanzada cuando el generador de HTML detecta una construcción
 * semánticamente inválida en el AST, por ejemplo el uso de la propiedad
 * {@code enlace} fuera de un componente {@code boton}.
 *
 * Es una excepción no verificada para no contaminar la interfaz {@link
 * com.alberto.webdsl.patronvisitor.ASTVisitor} con throws declarados.
 * El punto de entrada ({@code PruebaParser}) la captura y la muestra por separado.
 */
public class GenerationException extends RuntimeException {
    public GenerationException(String message) {
        super(message);
    }
}
