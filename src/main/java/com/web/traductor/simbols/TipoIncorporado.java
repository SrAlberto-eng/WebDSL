package com.web.traductor.simbols;

/**
 * Tipo predefinido del lenguaje Markup (por ejemplo: cadena, número).
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
