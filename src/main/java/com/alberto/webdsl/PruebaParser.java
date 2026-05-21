package com.alberto.webdsl;

import java.io.FileReader;
import java.io.IOException;

import com.alberto.webdsl.astnodes.NodoPagina;
import com.alberto.webdsl.lexer.LexicalException;
import com.alberto.webdsl.lexer.WebLexer;
import com.alberto.webdsl.parser.SyntaxException;
import com.alberto.webdsl.parser.WebParser;
import com.alberto.webdsl.simbols.Simbolo;
import com.alberto.webdsl.simbols.TablaSimbolos;
import com.alberto.webdsl.tokens.Token;

/**
 * Punto de entrada del traductor WebDSL.
 * Lee {@code input.webdsl}, ejecuta el análisis léxico e imprime
 * los tokens reconocidos. A medida que se implementen las fases
 * siguientes, este método orquestará la generación de HTML.
 */
public class PruebaParser {
    public static void main(String[] args) throws LexicalException, SyntaxException {
        String entrada = leerArchivo("src/main/java/com/alberto/webdsl/input.webdsl");

        WebLexer lexer = new WebLexer();
        lexer.analizar(entrada);
        System.out.println("*** Analisis lexico ***\n");
        for (Token t : lexer.getTokens())
            System.out.println(t);

        TablaSimbolos ts = new TablaSimbolos();
        WebParser parser = new WebParser(ts);
        NodoPagina pagina = parser.analizar(lexer);
        System.out.println("\n*** Parseo exitoso: pagina " + pagina.getNombre() + " ***");
        System.out.println("\n*** Tabla de símbolos ***");
        for (Simbolo s : ts.getSimbolos())
            System.out.println(s);
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
