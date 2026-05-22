package com.web.traductor.simbols;

import java.util.ArrayList;

import com.web.traductor.parser.SyntaxException;

/**
 * Tabla de símbolos utilizada durante el análisis semántico.
 * Registra cada variable declarada y verifica que no haya declaraciones
 * duplicadas ni referencias a variables no declaradas.
 */
public class TablaSimbolos {
    private final ArrayList<Simbolo> simbolos = new ArrayList<>();

    public void definir(Simbolo simbolo) throws SyntaxException {
        for (Simbolo s : simbolos)
            if (s.getNombre().equals(simbolo.getNombre()))
                throw new SyntaxException(" El símbolo " + s.getNombre() + " ya fue declarado");

        simbolos.add(simbolo);
    }

    public Simbolo resolver(String nombre) throws SyntaxException {
        for (Simbolo s : simbolos)
            if (s.getNombre().equals(nombre))
                return s;

        throw new SyntaxException(" El símbolo " + nombre + " no ha sido declarado");
    }

    public ArrayList<Simbolo> getSimbolos() {
        return simbolos;
    }
}
