package com.ioc.common;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
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
