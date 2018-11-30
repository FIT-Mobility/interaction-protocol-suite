package de.fraunhofer.fit.ips.model.xsd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

/**
 * Top-level definitions
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.01.2018
 */
public abstract class Type implements NamedConceptWithOrigin {

    @RequiredArgsConstructor
    @ToString
    @Getter
    public abstract static class Simple extends Type {
        private final QName name;
        private final Origin origin;
        private final Documentations docs;

        @ToString
        @Getter
        public static class Restriction extends Simple {
            @Nullable private final QName baseType; // restriction base of the simple type
            @Nullable private final String minMaxRange;

            public Restriction(final QName name, final Origin origin, final Documentations docs,
                               final @Nullable QName baseType, final @Nullable String minMaxRange) {
                super(name, origin, docs);
                this.baseType = baseType;
                this.minMaxRange = minMaxRange;
            }

            @Override
            public void accept(final NamedConceptWithOriginVisitor visitor) {
                visitor.visit(this);
            }
        }

        @ToString
        @Getter
        public static class Enumeration extends Simple {
            @RequiredArgsConstructor
            @Getter
            @ToString
            public static class Value {
                final Documentations docs;
                final String value;
            }

            private final QName baseType;
            private final java.util.List<Value> enumValues;

            public Enumeration(final QName name, final Origin origin, final Documentations docs,
                               final QName baseType, final java.util.List<Value> enumValues) {
                super(name, origin, docs);
                this.baseType = baseType;
                this.enumValues = enumValues;
            }

            @Override
            public void accept(final NamedConceptWithOriginVisitor visitor) {
                visitor.visit(this);
            }
        }

        @ToString
        @Getter
        public static class List extends Simple {
            public List(final QName name, final Origin origin, final Documentations docs) {
                super(name, origin, docs);
            }


            @Override
            public void accept(final NamedConceptWithOriginVisitor visitor) {
                visitor.visit(this);
            }
        }

        @ToString
        @Getter
        public static class Union extends Simple {
            public Union(final QName name, final Origin origin, final Documentations docs) {
                super(name, origin, docs);
            }

            @Override
            public void accept(final NamedConceptWithOriginVisitor visitor) {
                visitor.visit(this);
            }
        }
    }

    public static abstract class ComplexOrGroup extends Type {
        public abstract SequenceOrChoiceOrGroupRef getParticle();
    }

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static class Complex extends ComplexOrGroup {
        private final QName name;
        private final Origin origin;
        private final Derivation derivation;
        private final Documentations docs;

        @Setter private Attributes attributes;
        @Setter private SequenceOrChoiceOrGroupRef particle;

        @Override
        public void accept(final NamedConceptWithOriginVisitor visitor) {
            visitor.visit(this);
        }
    }

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static class Group extends ComplexOrGroup {
        private final QName name;
        private final Origin origin;
        private final Documentations docs;

        @Setter private SequenceOrChoice particle;

        @Override
        public void accept(final NamedConceptWithOriginVisitor visitor) {
            visitor.visit(this);
        }
    }
}
