package com.web.traductor.simbols;

/**
 * Tipo predefinido del lenguaje (cadena, número, etc.).
 * Se registra en la tabla de símbolos como un símbolo de tipo incorporado.
 */
public class TipoIncorporado extends Simbolo implements Tipo {
    public TipoIncorporado(String nombre) {
        super(nombre);
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
