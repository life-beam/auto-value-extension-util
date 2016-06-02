package com.gabrielittner.auto.value.util.extensions;

import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import static com.gabrielittner.auto.value.util.AutoValueUtil.newTypeSpecBuilder;

public class AbstractExtension extends AutoValueExtension {

    @Override
    public boolean applicable(Context context) {
        return true;
    }

    @Override
    public String generateClass(
            Context context, String className, String classToExtend, boolean isFinal) {
        TypeSpec subclass = newTypeSpecBuilder(context, className, classToExtend, isFinal).build();
        return JavaFile.builder(context.packageName(), subclass).build().toString();
    }
}
