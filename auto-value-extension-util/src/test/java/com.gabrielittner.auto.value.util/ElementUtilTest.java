package com.gabrielittner.auto.value.util;

import com.google.testing.compile.CompilationRule;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.google.auto.common.MoreElements.getLocalAndInheritedMethods;
import static com.google.common.truth.Truth.assertThat;
import static com.squareup.javapoet.TypeName.INT;
import static com.squareup.javapoet.TypeName.VOID;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class ElementUtilTest {
    
    private static final TypeName STRING = TypeName.get(String.class);

    @Rule public CompilationRule compilationRule = new CompilationRule();

    private Elements elements;

    @Before
    public void setUp() {
        this.elements = compilationRule.getElements();
    }

    @SuppressWarnings("unused")
    private static abstract class MethodTestClass {
        void a() {}
        abstract void b(String b);
        static int c() {
            return 0;
        }
        abstract int d(String d);
        static String e(int e) {
            return null;
        }
    }

    @Test
    @Deprecated
    @SuppressWarnings("deprecation")
    public void methodTests() {
        TypeElement element = elements.getTypeElement(MethodTestClass.class.getCanonicalName());

        // method a
        assertThat(ElementUtil.getAbstractMethod(elements, element, null, VOID)).isNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, null, VOID)).isFalse();
        assertThat(ElementUtil.getStaticMethod(element, null, VOID)).isNull();
        assertThat(ElementUtil.hasStaticMethod(element, null, VOID)).isFalse();

        // method b
        assertThat(ElementUtil.getAbstractMethod(elements, element, STRING, VOID)).isNotNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, STRING, VOID)).isTrue();
        assertThat(ElementUtil.getStaticMethod(element, STRING, VOID)).isNull();
        assertThat(ElementUtil.hasStaticMethod(element, STRING, VOID)).isFalse();

        // method c
        assertThat(ElementUtil.getAbstractMethod(elements, element, null, INT)).isNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, null, INT)).isFalse();
        assertThat(ElementUtil.getStaticMethod(element, null, INT)).isNotNull();
        assertThat(ElementUtil.hasStaticMethod(element, null, INT)).isTrue();

        // method d
        assertThat(ElementUtil.getAbstractMethod(elements, element, STRING, INT)).isNotNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, STRING, INT)).isTrue();
        assertThat(ElementUtil.getStaticMethod(element, STRING, INT)).isNull();
        assertThat(ElementUtil.hasStaticMethod(element, STRING, INT)).isFalse();

        // method e
        assertThat(ElementUtil.getAbstractMethod(elements, element, INT, STRING)).isNull();
        assertThat(ElementUtil.hasAbstractMethod(elements, element, INT, STRING)).isFalse();
        assertThat(ElementUtil.getStaticMethod(element, INT, STRING)).isNotNull();
        assertThat(ElementUtil.hasStaticMethod(element, INT, STRING)).isTrue();
    }

    @Test
    public void matchingMethodTests() {
        TypeElement element = elements.getTypeElement(MethodTestClass.class.getCanonicalName());
        Set<ExecutableElement> methods = getLocalAndInheritedMethods(element, elements);

        // method a
        assertThat(ElementUtil.getMatchingAbstractMethod(methods, VOID).isPresent())
                .isFalse();
        assertThat(ElementUtil.getMatchingStaticMethod(element, VOID).isPresent())
                .isFalse();

        // method b
        assertThat(ElementUtil.getMatchingAbstractMethod(methods, VOID, STRING).isPresent())
                .isTrue();
        assertThat(ElementUtil.getMatchingStaticMethod(element, VOID, STRING).isPresent())
                .isFalse();

        // method c
        assertThat(ElementUtil.getMatchingAbstractMethod(methods, INT).isPresent()).isFalse();
        assertThat(ElementUtil.getMatchingStaticMethod(element, INT).isPresent()).isTrue();

        // method d
        assertThat(ElementUtil.getMatchingAbstractMethod(methods, INT, STRING).isPresent())
                .isTrue();
        assertThat(ElementUtil.getMatchingStaticMethod(element, INT, STRING).isPresent())
                .isFalse();

        // method e
        assertThat(ElementUtil.getMatchingAbstractMethod(methods, STRING, INT).isPresent())
                .isFalse();
        assertThat(ElementUtil.getMatchingStaticMethod(element, STRING, INT).isPresent())
                .isTrue();
    }

    @SuppressWarnings("unused")
    private static abstract class MethodModifierTestClass {
        void a() {}
        abstract void b();
        static void c() {}
        public void d() {}
        public abstract void e();
        public static void f() {}
    }

    @Test
    public void hasModifier() {
        TypeElement element =
                elements.getTypeElement(MethodModifierTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.hasModifier(a, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(a, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(a, Modifier.PUBLIC)).isFalse();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.hasModifier(b, Modifier.ABSTRACT)).isTrue();
        assertThat(ElementUtil.hasModifier(b, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(b, Modifier.PUBLIC)).isFalse();

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.hasModifier(c, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(c, Modifier.STATIC)).isTrue();
        assertThat(ElementUtil.hasModifier(c, Modifier.PUBLIC)).isFalse();

        ExecutableElement d = (ExecutableElement) getElementWithName(elements, "d");
        assertThat(ElementUtil.hasModifier(d, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(d, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(d, Modifier.PUBLIC)).isTrue();

        ExecutableElement e = (ExecutableElement) getElementWithName(elements, "e");
        assertThat(ElementUtil.hasModifier(e, Modifier.ABSTRACT)).isTrue();
        assertThat(ElementUtil.hasModifier(e, Modifier.STATIC)).isFalse();
        assertThat(ElementUtil.hasModifier(e, Modifier.PUBLIC)).isTrue();

        ExecutableElement f = (ExecutableElement) getElementWithName(elements, "f");
        assertThat(ElementUtil.hasModifier(f, Modifier.ABSTRACT)).isFalse();
        assertThat(ElementUtil.hasModifier(f, Modifier.STATIC)).isTrue();
        assertThat(ElementUtil.hasModifier(f, Modifier.PUBLIC)).isTrue();
    }

    @SuppressWarnings("unused")
    private static class MethodReturnsTestClass {
        void a() {}
        int b() {
            return 0;
        }
        String c() {
            return null;
        }
    }

    @Test
    public void methodReturns() {
        TypeElement element =
                elements.getTypeElement(MethodReturnsTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.methodReturns(a, VOID)).isTrue();
        assertThat(ElementUtil.methodReturns(a, INT)).isFalse();
        assertThat(ElementUtil.methodReturns(a, STRING)).isFalse();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.methodReturns(b, VOID)).isFalse();
        assertThat(ElementUtil.methodReturns(b, INT)).isTrue();
        assertThat(ElementUtil.methodReturns(b, STRING)).isFalse();

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.methodReturns(c, VOID)).isFalse();
        assertThat(ElementUtil.methodReturns(c, INT)).isFalse();
        assertThat(ElementUtil.methodReturns(c, STRING)).isTrue();
    }

    @SuppressWarnings("unused")
    private static class MethodTakesTestClass {
        void a() {}
        void b(int b) {}
        void c(String c) {}
        void d(int d, int d2) {}
        void e(String e, String e2) {}
    }

    @Test
    public void methodTakes() {
        TypeElement element =
                elements.getTypeElement(MethodTakesTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.methodTakes(a)).isTrue();
        assertThat(ElementUtil.methodTakes(a, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(a, STRING)).isFalse();
        assertThat(ElementUtil.methodTakes(a, INT, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(a, STRING, STRING)).isFalse();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.methodTakes(b)).isFalse();
        assertThat(ElementUtil.methodTakes(b, INT)).isTrue();
        assertThat(ElementUtil.methodTakes(b, STRING)).isFalse();
        assertThat(ElementUtil.methodTakes(b, INT, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(b, STRING, STRING)).isFalse();

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.methodTakes(c)).isFalse();
        assertThat(ElementUtil.methodTakes(c, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(c, STRING)).isTrue();
        assertThat(ElementUtil.methodTakes(c, INT, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(c, STRING, STRING)).isFalse();

        ExecutableElement d = (ExecutableElement) getElementWithName(elements, "d");
        assertThat(ElementUtil.methodTakes(d)).isFalse();
        assertThat(ElementUtil.methodTakes(d, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(d, STRING)).isFalse();
        assertThat(ElementUtil.methodTakes(d, INT, INT)).isTrue();
        assertThat(ElementUtil.methodTakes(d, STRING, STRING)).isFalse();

        ExecutableElement e = (ExecutableElement) getElementWithName(elements, "e");
        assertThat(ElementUtil.methodTakes(e)).isFalse();
        assertThat(ElementUtil.methodTakes(e, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(e, STRING)).isFalse();
        assertThat(ElementUtil.methodTakes(e, INT, INT)).isFalse();
        assertThat(ElementUtil.methodTakes(e, STRING, STRING)).isTrue();
    }

    @Test
    public void typeExists() {
        ClassName testClassName = ClassName.get(AnnotationTestClass.class);
        assertThat(ElementUtil.typeExists(elements, testClassName)).isTrue();
        ClassName testClass2Name = testClassName.peerClass("TestClass2");
        assertThat(ElementUtil.typeExists(elements, testClass2Name)).isFalse();
    }

    @SuppressWarnings("unused")
    private static abstract class AnnotationTestClass {
        public abstract int a();
        @Annotation1 public abstract int b();
        @Annotation1 @Annotation2("test") public abstract int c();
    }

    @Retention(RUNTIME)
    @Target({METHOD, FIELD})
    private @interface Annotation1 {}

    @Retention(RUNTIME)
    @Target({METHOD, FIELD})
    @SuppressWarnings("unused")
    private @interface Annotation2 {
        String value();
    }

    @Test
    public void annotationWithName() {
        TypeElement element = elements.getTypeElement(AnnotationTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        Element a = getElementWithName(elements, "a");
        assertThat(ElementUtil.hasAnnotationWithName(a, "Annotation1")).isFalse();
        assertThat(ElementUtil.hasAnnotationWithName(a, "Annotation2")).isFalse();

        Element b = getElementWithName(elements, "b");
        assertThat(ElementUtil.hasAnnotationWithName(b, "Annotation1")).isTrue();
        assertThat(ElementUtil.hasAnnotationWithName(b, "Annotation2")).isFalse();

        Element c = getElementWithName(elements, "c");
        assertThat(ElementUtil.hasAnnotationWithName(c, "Annotation1")).isTrue();
        assertThat(ElementUtil.hasAnnotationWithName(c, "Annotation2")).isTrue();
    }

    @Test
    public void buildAnnotationsTest() {
        TypeElement element = elements.getTypeElement(AnnotationTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        ExecutableElement a = (ExecutableElement) getElementWithName(elements, "a");
        assertThat(ElementUtil.buildAnnotations(a)).isEmpty();

        ExecutableElement b = (ExecutableElement) getElementWithName(elements, "b");
        assertThat(ElementUtil.buildAnnotations(b)).containsExactly("Annotation1");

        ExecutableElement c = (ExecutableElement) getElementWithName(elements, "c");
        assertThat(ElementUtil.buildAnnotations(c)).containsExactly("Annotation1", "Annotation2");
    }

    @Test
    public void annotationValueTest() {
        TypeElement element = elements.getTypeElement(AnnotationTestClass.class.getCanonicalName());
        List<? extends Element> elements = element.getEnclosedElements();

        Element a = getElementWithName(elements, "a");
        assertThat(ElementUtil.getAnnotationValue(a, Annotation2.class, "value")).isEqualTo(null);

        Element c = getElementWithName(elements, "c");
        assertThat(ElementUtil.getAnnotationValue(c, Annotation2.class, "value")).isEqualTo("test");

        try {
            ElementUtil.getAnnotationValue(c, Annotation2.class, "value2");
        } catch (Throwable throwable) {
            assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
        }
    }

    private Element getElementWithName(List<? extends Element> elements, String name) {
        for (Element element : elements) {
            if (element.getSimpleName().toString().equals(name)) {
                return element;
            }
        }
        throw new IllegalArgumentException("Element with name '" + name + "' not found");
    }
}
