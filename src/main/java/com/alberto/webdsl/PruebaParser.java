package com.alberto.webdsl;

import java.io.FileWriter;
import java.nio.file.Path;

import com.alberto.webdsl.astnodes.NodoPagina;
import com.alberto.webdsl.lexer.LexicalException;
import com.alberto.webdsl.lexer.WebLexer;
import com.alberto.webdsl.parser.SyntaxException;
import com.alberto.webdsl.parser.WebParser;
import com.alberto.webdsl.simbols.Simbolo;
import com.alberto.webdsl.simbols.TablaSimbolos;
import com.alberto.webdsl.traductor.GenerationException;
import com.alberto.webdsl.traductor.TraductorHtml;

/**
 * Punto de entrada del traductor WebDSL.
 * Lee {@code input.webdsl}, ejecuta el pipeline completo (léxico → sintáctico →
 * generación) y escribe el resultado en {@code output.html}.
 */
public class PruebaParser {
    private static final String INPUT  = "src/main/java/com/alberto/webdsl/input.webdsl";
    private static final String OUTPUT = "docs/index.html";

    public static void main(String[] args) {
        try {
            String entrada = leerArchivo(INPUT);

            // Análisis léxico
            WebLexer lexer = new WebLexer();
            lexer.analizar(entrada);
            System.out.println("*** Análisis léxico: " + lexer.getTokens().size() + " tokens ***");

            // Análisis sintáctico
            TablaSimbolos ts = new TablaSimbolos();
            NodoPagina pagina = new WebParser(ts).analizar(lexer);
            System.out.println("*** Parseo exitoso: pagina " + pagina.getNombre() + " ***");
            System.out.println("*** Tabla de símbolos ***");
            for (Simbolo s : ts.getSimbolos())
                System.out.println("    " + s);

            // Generación de HTML
            String html = new TraductorHtml().generar(pagina);
            escribirArchivo(OUTPUT, html);
            System.out.println("\n*** HTML generado en " + Path.of(OUTPUT).toAbsolutePath() + " ***");

        } catch (LexicalException e) {
            System.err.println("\n[ERROR LÉXICO] " + e.getMessage());
        } catch (SyntaxException e) {
            System.err.println("\n[ERROR SINTÁCTICO] " + e.getMessage());
        } catch (GenerationException e) {
            System.err.println("\n[ERROR DE GENERACIÓN] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n[ERROR] " + e.getMessage());
        }
    }

    private static String leerArchivo(String ruta) throws Exception {
        return java.nio.file.Files.readString(Path.of(ruta));
    }

    private static void escribirArchivo(String ruta, String contenido) throws Exception {
        try (FileWriter fw = new FileWriter(ruta)) {
            fw.write(contenido);
        }
    }
}
