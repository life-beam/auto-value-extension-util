package com.gabrielittner.auto.value.util.extensions;

public class SimpleFinalAutoValueExtension extends SimpleAutoValueExtension {

    @Override public boolean mustBeFinal(Context context) {
        return true;
    }
}
