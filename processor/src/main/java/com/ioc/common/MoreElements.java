package com.ioc.common;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor6;

import static javax.lang.model.element.ElementKind.PACKAGE;

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

public class MoreElements {

    public static PackageElement getPackage(Element element) {
        while (element.getKind() != PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element;
    }

    public static TypeElement asType(Element element) {
        return element.accept(TypeElementVisitor.INSTANCE, null);
    }

    public static boolean isType(Element element) {
        return element.getKind().isClass() || element.getKind().isInterface();
    }

    public static boolean isExecutable(Element element) {
        return element instanceof ExecutableElement;
    }

    public static ExecutableElement asExecutable(Element element) {
        return element.accept(ExecutableElementVisitor.INSTANCE, null);
    }

    public static PackageElement asPackage(Element element) {
        return element.accept(PackageElementVisitor.INSTANCE, null);
    }

    private static final class PackageElementVisitor extends CastingElementVisitor<PackageElement> {
        private static final PackageElementVisitor INSTANCE = new PackageElementVisitor();

        PackageElementVisitor() {
            super("package element");
        }

        @Override
        public PackageElement visitPackage(PackageElement e, Void ignore) {
            return e;
        }
    }

    private static final class ExecutableElementVisitor
            extends CastingElementVisitor<ExecutableElement> {
        private static final ExecutableElementVisitor INSTANCE = new ExecutableElementVisitor();

        ExecutableElementVisitor() {
            super("executable element");
        }

        @Override
        public ExecutableElement visitExecutable(ExecutableElement e, Void label) {
            return e;
        }
    }

    private static final class TypeElementVisitor extends CastingElementVisitor<TypeElement> {
        private static final TypeElementVisitor INSTANCE = new TypeElementVisitor();

        TypeElementVisitor() {
            super("type element");
        }

        @Override
        public TypeElement visitType(TypeElement e, Void ignore) {
            return e;
        }
    }

    private abstract static class CastingElementVisitor<T> extends SimpleElementVisitor6<T, Void> {
        private final String label;

        CastingElementVisitor(String label) {
            this.label = label;
        }

        @Override
        protected final T defaultAction(Element e, Void ignore) {
            throw new IllegalArgumentException(e + " does not represent a " + label);
        }
    }
}
