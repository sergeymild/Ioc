package com.ioc.common;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Created by sergeygolishnikov on 31/10/2017.
 */

public class MoreTypes {
    /**
     * Returns the type of the innermost enclosing instance, or null if there is none. This is the
     * same as {@link DeclaredType#getEnclosingType()} except that it returns null rather than
     * NoType for a static type. We need this because of
     * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=508222">this bug</a> whereby
     * the Eclipse compiler returns a value for static classes that is not NoType.
     */
    static TypeMirror enclosingType(DeclaredType t) {
        TypeMirror enclosing = t.getEnclosingType();
        if (enclosing.getKind().equals(TypeKind.NONE)
                || t.asElement().getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        return enclosing;
    }

    private static boolean isIntersectionType(TypeMirror t) {
        return t != null && t.getKind().name().equals("INTERSECTION");
    }

    static Element asElement(TypeMirror typeMirror) {
        return typeMirror.accept(AsElementVisitor.INSTANCE, null);
    }

    static TypeElement asTypeElement(TypeMirror mirror) {
        return MoreElements.asType(asElement(mirror));
    }

    static DeclaredType asDeclared(TypeMirror maybeDeclaredType) {
        return maybeDeclaredType.accept(DeclaredTypeVisitor.INSTANCE, null);
    }

    static ExecutableType asExecutable(TypeMirror maybeExecutableType) {
        return maybeExecutableType.accept(ExecutableTypeVisitor.INSTANCE, null);
    }

    public static boolean isType(TypeMirror type) {
        return type.accept(IsTypeVisitor.INSTANCE, null);
    }

    /**
     * Returns true if the raw type underlying the given {@link TypeMirror} represents the same raw
     * type as the given {@link Class} and throws an IllegalArgumentException if the {@link
     * TypeMirror} does not represent a type that can be referenced by a {@link Class}
     */
    public static boolean isTypeOf(final Class<?> clazz, TypeMirror type) {
        return type.accept(new IsTypeOf(clazz), null);
    }

    private static final class IsTypeOf extends SimpleTypeVisitor6<Boolean, Void> {
        private final Class<?> clazz;

        IsTypeOf(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        protected Boolean defaultAction(TypeMirror type, Void ignored) {
            throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
        }

        @Override
        public Boolean visitNoType(NoType noType, Void p) {
            if (noType.getKind().equals(TypeKind.VOID)) {
                return clazz.equals(Void.TYPE);
            }
            throw new IllegalArgumentException(noType + " cannot be represented as a Class<?>.");
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType type, Void p) {
            switch (type.getKind()) {
                case BOOLEAN:
                    return clazz.equals(Boolean.TYPE);
                case BYTE:
                    return clazz.equals(Byte.TYPE);
                case CHAR:
                    return clazz.equals(Character.TYPE);
                case DOUBLE:
                    return clazz.equals(Double.TYPE);
                case FLOAT:
                    return clazz.equals(Float.TYPE);
                case INT:
                    return clazz.equals(Integer.TYPE);
                case LONG:
                    return clazz.equals(Long.TYPE);
                case SHORT:
                    return clazz.equals(Short.TYPE);
                default:
                    throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
            }
        }

        @Override
        public Boolean visitArray(ArrayType array, Void p) {
            return clazz.isArray() && isTypeOf(clazz.getComponentType(), array.getComponentType());
        }

        @Override
        public Boolean visitDeclared(DeclaredType type, Void ignored) {
            TypeElement typeElement;
            try {
                typeElement = MoreElements.asType(type.asElement());
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException(type + " does not represent a class or interface.");
            }
            return typeElement.getQualifiedName().contentEquals(clazz.getCanonicalName());
        }
    }

    private static final class IsTypeVisitor extends SimpleTypeVisitor6<Boolean, Void> {
        private static final IsTypeVisitor INSTANCE = new IsTypeVisitor();

        @Override
        protected Boolean defaultAction(TypeMirror type, Void ignored) {
            return false;
        }

        @Override
        public Boolean visitNoType(NoType noType, Void p) {
            return noType.getKind().equals(TypeKind.VOID);
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType type, Void p) {
            return true;
        }

        @Override
        public Boolean visitArray(ArrayType array, Void p) {
            return true;
        }

        @Override
        public Boolean visitDeclared(DeclaredType type, Void ignored) {
            return MoreElements.isType(type.asElement());
        }
    }

    private static final class ExecutableTypeVisitor extends CastingTypeVisitor<ExecutableType> {
        private static final ExecutableTypeVisitor INSTANCE = new ExecutableTypeVisitor();

        ExecutableTypeVisitor() {
            super("executable type");
        }

        @Override
        public ExecutableType visitExecutable(ExecutableType type, Void ignore) {
            return type;
        }
    }

    private static final class DeclaredTypeVisitor extends CastingTypeVisitor<DeclaredType> {
        private static final DeclaredTypeVisitor INSTANCE = new DeclaredTypeVisitor();

        DeclaredTypeVisitor() {
            super("declared type");
        }

        @Override
        public DeclaredType visitDeclared(DeclaredType type, Void ignore) {
            return type;
        }
    }


    private static final class AsElementVisitor extends SimpleTypeVisitor6<Element, Void> {
        private static final AsElementVisitor INSTANCE = new AsElementVisitor();

        @Override
        protected Element defaultAction(TypeMirror e, Void p) {
            throw new IllegalArgumentException(e + " cannot be converted to an Element");
        }

        @Override
        public Element visitDeclared(DeclaredType t, Void p) {
            return t.asElement();
        }

        @Override
        public Element visitError(ErrorType t, Void p) {
            return t.asElement();
        }

        @Override
        public Element visitTypeVariable(TypeVariable t, Void p) {
            return t.asElement();
        }
    }

    /**
     * Returns the set of {@linkplain TypeElement types} that are referenced by the given {@link
     * TypeMirror}.
     */
    public static Set<TypeElement> referencedTypes(TypeMirror type) {

        Set<TypeElement> elements = new HashSet<TypeElement>();
        type.accept(ReferencedTypes.INSTANCE, elements);
        return elements;
    }

    private static final class ReferencedTypes extends SimpleTypeVisitor6<Void, Set<TypeElement>> {
        private static final ReferencedTypes INSTANCE = new ReferencedTypes();

        @Override
        public Void visitArray(ArrayType t, Set<TypeElement> p) {
            t.getComponentType().accept(this, p);
            return null;
        }

        @Override
        public Void visitDeclared(DeclaredType t, Set<TypeElement> p) {
            p.add(MoreElements.asType(t.asElement()));
            for (TypeMirror typeArgument : t.getTypeArguments()) {
                typeArgument.accept(this, p);
            }
            return null;
        }

        @Override
        public Void visitTypeVariable(TypeVariable t, Set<TypeElement> p) {
            t.getLowerBound().accept(this, p);
            t.getUpperBound().accept(this, p);
            return null;
        }

        @Override
        public Void visitWildcard(WildcardType t, Set<TypeElement> p) {
            TypeMirror extendsBound = t.getExtendsBound();
            if (extendsBound != null) {
                extendsBound.accept(this, p);
            }
            TypeMirror superBound = t.getSuperBound();
            if (superBound != null) {
                superBound.accept(this, p);
            }
            return null;
        }
    }

    private abstract static class CastingTypeVisitor<T> extends SimpleTypeVisitor6<T, Void> {
        private final String label;

        CastingTypeVisitor(String label) {
            this.label = label;
        }

        @Override
        protected T defaultAction(TypeMirror e, Void v) {
            throw new IllegalArgumentException(e + " does not represent a " + label);
        }
    }
}
