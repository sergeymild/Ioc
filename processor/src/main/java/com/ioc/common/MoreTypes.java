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

    static Element asElement(TypeMirror typeMirror) {
        return typeMirror.accept(AsElementVisitor.INSTANCE, null);
    }

    static TypeElement asTypeElement(TypeMirror mirror) {
        return MoreElements.asType(asElement(mirror));
    }

    static DeclaredType asDeclared(TypeMirror maybeDeclaredType) {
        return maybeDeclaredType.accept(DeclaredTypeVisitor.INSTANCE, null);
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
