package com.alberto.webdsl;

import java.io.FileReader;
import java.io.IOException;

import com.alberto.webdsl.lexer.LexicalException;
import com.alberto.webdsl.lexer.WebLexer;
import com.alberto.webdsl.tokens.Token;

public class PruebaParser {
    public static void main(String[] args) throws LexicalException {
        String entrada = leerArchivo("src/main/java/com/alberto/webdsl/input.webdsl");
        WebLexer lexer = new WebLexer();
        lexer.analizar(entrada);

        System.out.println("*** Análisis léxico ***\n");
        for (Token t : lexer.getTokens())
            System.out.println(t);
    }

    private static String leerArchivo(String ruta) {
        StringBuilder sb = new StringBuilder();
        try (FileReader reader = new FileReader(ruta)) {
            int c;
            while ((c = reader.read()) != -1)
                sb.append((char) c);
        } catch (IOException e) {
            System.err.println("Error leyendo archivo: " + e.getMessage());
        }
        return sb.toString();
    }
}
