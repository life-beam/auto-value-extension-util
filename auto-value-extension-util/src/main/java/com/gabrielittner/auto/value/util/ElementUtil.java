package com.gabrielittner.auto.value.util;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Annotation;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import static com.google.auto.common.MoreElements.getLocalAndInheritedMethods;

public final class ElementUtil {

    /**
     * Returns true if {@code cls} has a static method and has {@code returns} as return type.
     * If {@code takes} is not null the method has to have exactly one parameter with that type,
     * otherwise zero parameters.
     *
     * @deprecated use {@link #hasMatchingStaticMethod(TypeElement, TypeName, TypeName...)}
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean hasStaticMethod(TypeElement cls, TypeName takes, TypeName returns) {
        return hasMatchingStaticMethod(cls, returns, toArray(takes));
    }

    /**
     * Returns a method of {@code cls} that is static and has {@code returns} as return type.
     * If {@code takes} is not null the method has to have exactly one parameter with that type,
     * otherwise zero parameters.
     * Returns null if such a method doesn't exist.
     *
     * @deprecated use {@link #getMatchingStaticMethod(TypeElement, TypeName, TypeName...)}
     */
    @SuppressWarnings("WeakerAccess")
    public static ExecutableElement getStaticMethod(TypeElement cls, TypeName takes,
            TypeName returns) {
        return getMatchingStaticMethod(cls, returns, toArray(takes));
    }

    /**
     * Returns true if {@code cls} has an abstract method and has {@code returns} as return type.
     * If {@code takes} is not null the method has to have exactly one parameter with that type,
     * otherwise zero parameters.
     *
     * @deprecated use {@link #hasMatchingAbstractMethod(Elements, TypeElement, TypeName, TypeName...)}
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean hasAbstractMethod(Elements elementUtils, TypeElement cls, TypeName takes,
            TypeName returns) {
        return hasMatchingAbstractMethod(elementUtils, cls, returns, toArray(takes));
    }

    /**
     * Returns a method of {@code cls} that is abstract and has {@code returns} as return type.
     * If {@code takes} is not null the method has to have exactly one parameter with that type,
     * otherwise zero parameters.
     * Returns null if such a method doesn't exist.
     *
     * @deprecated use {@link #getMatchingAbstractMethod(Elements, TypeElement, TypeName, TypeName...)}
     */
    @SuppressWarnings("WeakerAccess")
    public static ExecutableElement getAbstractMethod(Elements elementUtils,
            TypeElement cls, TypeName takes, TypeName returns) {
        return getMatchingAbstractMethod(elementUtils, cls, returns, toArray(takes));
    }
    
    private static TypeName[] toArray(TypeName typeName) {
        return typeName != null ? new TypeName[] { typeName } : new TypeName[0];
    }

    /**
     * Returns true if {@code cls} has a static method, has {@code returns} as return type
     * and the number and types of parameters match {@code takes}.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean hasMatchingStaticMethod(TypeElement cls, TypeName returns,
            TypeName... takes) {
        return getMatchingStaticMethod(cls, returns, takes) != null;
    }

    /**
     * Returns a method of {@code cls} that is static, has {@code returns} as return type
     * and the number and types of parameters match {@code takes}.
     * Returns null if such a method doesn't exist.
     */
    @SuppressWarnings("WeakerAccess")
    public static ExecutableElement getMatchingStaticMethod(TypeElement cls, TypeName returns,
            TypeName... takes) {
        for (Element element : cls.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) element;
            if (methodMatches(method, Modifier.STATIC, returns, takes)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Returns true if {@code cls} has an abstract method, has {@code returns} as return type
     * and the number and types of parameters match {@code takes}.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean hasMatchingAbstractMethod(Elements elementUtils, TypeElement cls,
            TypeName returns, TypeName... takes) {
        return getMatchingAbstractMethod(elementUtils, cls, returns, takes) != null;
    }

    /**
     * Returns a method of {@code cls} that is abstract, has {@code returns} as return type
     * and the number and types of parameters match {@code takes}.
     * Returns null if such a method doesn't exist.
     */
    @SuppressWarnings("WeakerAccess")
    public static ExecutableElement getMatchingAbstractMethod(Elements elementUtils,
            TypeElement cls, TypeName returns, TypeName... takes) {
        for (ExecutableElement method : getLocalAndInheritedMethods(cls, elementUtils)) {
            if (methodMatches(method, Modifier.ABSTRACT, returns, takes)) {
                return method;
            }
        }
        return null;
    }

    private static boolean methodMatches(ExecutableElement method, Modifier modifier,
            TypeName returns, TypeName[] takes) {
        return hasModifier(method, modifier) && methodTakes(method, takes)
                && methodReturns(method, returns);
    }

    static boolean hasModifier(ExecutableElement method, Modifier modifier) {
        return method.getModifiers().contains(modifier);
    }

    static boolean methodTakes(ExecutableElement method, TypeName... takes) {
        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() != takes.length) {
            return false;
        }
        for (int i = 0; i < takes.length; i++) {
            if (!takes[i].equals(TypeName.get(parameters.get(i).asType()))) {
                return false;
            }
        }
        return true;
    }

    static boolean methodReturns(ExecutableElement method, TypeName returns) {
        return returns.equals(ClassName.get(method.getReturnType()));
    }

    /**
     * Returns true if given {@code className} is on the current classpath.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean typeExists(Elements elements, ClassName className) {
        String name = className.toString();
        return elements.getTypeElement(name) != null;
    }

    /**
     * Returns true if the given {@code element} is annotated with an annotation
     * named {@code simpleName}.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean hasAnnotationWithName(Element element, String simpleName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            String name = mirror.getAnnotationType().asElement().getSimpleName().toString();
            if (simpleName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a {@link ImmutableSet} containing the names of all annotations of the given
     * {@code element}.
     */
    @SuppressWarnings("WeakerAccess")
    public static ImmutableSet<String> buildAnnotations(ExecutableElement element) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            builder.add(annotation.getAnnotationType().asElement().getSimpleName().toString());
        }
        return builder.build();
    }

    /**
     * If the given {@code element} is annotated with an {@link Annotation} of class {@code clazz}
     * it's value for {@code key} will be returned. Otherwise it will return null.
     *
     * @throws IllegalArgumentException if no element is defined with the given key.
     */
    @SuppressWarnings("WeakerAccess")
    public static Object getAnnotationValue(Element element, Class<? extends Annotation> clazz,
            String key) {
        Optional<AnnotationMirror> annotation = MoreElements.getAnnotationMirror(element, clazz);
        if (annotation.isPresent()) {
            return AnnotationMirrors.getAnnotationValue(annotation.get(), key).getValue();
        }
        return null;
    }

    private ElementUtil() {
        throw new AssertionError("No instances.");
    }
}
