package com.alberto.webdsl.simbols;

public class TipoIncorporado extends Simbolo implements Tipo {
    public TipoIncorporado(String nombre) {
        super(nombre);
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
