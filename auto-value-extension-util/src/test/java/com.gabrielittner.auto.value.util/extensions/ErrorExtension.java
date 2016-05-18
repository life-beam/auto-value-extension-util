package com.gabrielittner.auto.value.util.extensions;

import com.gabrielittner.auto.value.util.Property;
import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import static com.gabrielittner.auto.value.util.AutoValueUtil.error;
import static com.gabrielittner.auto.value.util.AutoValueUtil.newTypeSpecBuilder;

@AutoService(AutoValueExtension.class)
public class ErrorExtension extends AutoValueExtension {

    @Override public boolean applicable(Context context) {
        return true;
    }

    @Override public String generateClass(Context context, String className,
            String classToExtend, boolean isFinal) {
        ImmutableList<Property> properties = Property.buildProperties(context);
        Property property = properties.get(0);
        error(context, property, "Error generating %s extending %s with isFinal = %b", className,
                classToExtend, isFinal);
        TypeSpec subclass = newTypeSpecBuilder(context, className, classToExtend, isFinal).build();
        return JavaFile.builder(context.packageName(), subclass)
                .build()
                .toString();
    }
}
