package com.gabrielittner.auto.value.util;

import com.google.auto.value.extension.AutoValueExtension;
import com.google.auto.value.processor.Optionalish;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.TypeName;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * A Property of the AutoValue annotated class.
 *
 * This convenience class wraps the {@link ExecutableElement} for properties provided by the
 * AutoValueExtension.Context to make accessing values easier.
 *
 * It is recommended that you get the {@link #humanName} directly from the properties returned from
 * the AutoValueExtension.Context#properties method.
 *
 * <pre>
 * ImmutableList.Builder<Property> values = ImmutableList.builder();
 * for (Map.Entry<String, ExecutableElement> entry : context.properties().entrySet()) {
 *   values.add(new Property(entry.getKey(), entry.getValue()));
 * }
 * return values.build();
 * </pre>
 */
public class Property {

    /**
     * Builds a List of {@link Property} for the given {@link AutoValueExtension.Context}.
     */
    public static ImmutableList<Property> buildProperties(AutoValueExtension.Context context) {
        ImmutableList.Builder<Property> values = ImmutableList.builder();
        for (Map.Entry<String, ExecutableElement> entry : context.properties().entrySet()) {
            values.add(new Property(context.processingEnvironment(), entry.getKey(), entry.getValue()));
        }
        return values.build();
    }

    private final String methodName;
    private final String humanName;
    private final ExecutableElement element;
    private final TypeName type;
    private final TypeName returnType;
    private final Optionalish optionalish;
    private final ImmutableSet<String> annotations;

    public Property(ProcessingEnvironment processingEnvironment,
                    String humanName,
                    ExecutableElement element) {
        this.methodName = element.getSimpleName().toString();
        this.humanName = humanName;
        this.element = element;

        TypeMirror returnTypeMirror = element.getReturnType();
        optionalish = Optionalish.createIfOptional(returnTypeMirror, "$T");
        if (optionalish != null) {
            type = TypeName.get(optionalish.getContainedType(processingEnvironment.getTypeUtils()));
            returnType = TypeName.get(returnTypeMirror);
        } else {
            type = returnType = TypeName.get(returnTypeMirror);
        }

        annotations = ElementUtil.buildAnnotations(element);
    }

    /**
     * The method name of the property.
     */
    public String methodName() {
        return methodName;
    }

    /**
     * The human readable name of the property. If all properties use {@code get} or {@code is}
     * prefixes, this name will be different from {@link #methodName()}.
     */
    public String humanName() {
        return humanName;
    }

    /**
     * The underlying ExecutableElement representing the get method of the property.
     */
    public ExecutableElement element() {
        return element;
    }

    /**
     * The type of the property.
     *
     * Note that if the property is {@code optional}, this method returns the {@code TypeName}
     * of the <em>value</em> of the optional. Use {@link #returnType()} to get the actual return
     * type of the property.
     *
     * {@code Optional} properties are properties of any of the following types:
     *
     * <ul>
     *     {@link java.util.Optional}
     *     {@link java.util.OptionalDouble}
     *     {@link java.util.OptionalInt}
     *     {@link java.util.OptionalLong}
     *     {@link com.google.common.base.Optional}
     * </ul>
     */
    public TypeName type() {
        return type;
    }

    /**
     * The return type of the property.
     */
    public TypeName returnType() {
        return returnType;
    }

    /**
     * The code for getting an empty/absent instance of the {@code Optional} type of this property,
     * if the property is optional.
     *
     * For example, if the property's return type is {@link java.util.Optional}, this method will
     * return {@code $T.empty()}.
     *
     * If the property is not optional, this method returns null.
     */
    public String optionalEmpty() {
        return optionalish != null ? optionalish.getEmpty() : null;
    }

    /**
     * The set of annotations present on the original property.
     */
    public Set<String> annotations() {
        return annotations;
    }

    /**
     * True if the property can be null.
     */
    public Boolean nullable() {
        return annotations.contains("Nullable");
    }

    /**
     * True if the property is optional.
     *
     * @see #type()
     */
    public boolean optional() {
        return optionalish != null;
    }
}
