package de.fraunhofer.fit.ips.model.xsd;

import com.google.common.collect.Sets;
import de.fraunhofer.fit.ips.Visitable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@ToString
@Getter
public class Attributes {

    public interface AttributeDefaultOrFixedValue {
        @Nullable
        String getDefaultValue();

        @Nullable
        String getFixedValue();

        AttributeDefaultOrFixedValue NONE = new AttributeDefaultOrFixedValue() {
            @Nullable
            @Override
            public String getDefaultValue() {
                return null;
            }

            @Nullable
            @Override
            public String getFixedValue() {
                return null;
            }
        };

        static AttributeDefaultOrFixedValue createDefault(final String defaultValue) {
            return new AttributeDefaultOrFixedValue() {
                @Nullable
                @Override
                public String getDefaultValue() {
                    return defaultValue;
                }

                @Nullable
                @Override
                public String getFixedValue() {
                    return null;
                }
            };
        }

        static AttributeDefaultOrFixedValue createFixed(final String fixedValue) {
            return new AttributeDefaultOrFixedValue() {
                @Nullable
                @Override
                public String getDefaultValue() {
                    return null;
                }

                @Nullable
                @Override
                public String getFixedValue() {
                    return fixedValue;
                }
            };
        }
    }


    @RequiredArgsConstructor
    @ToString
    @Getter
    public static class GlobalAttributeGroupDeclaration implements NamedConceptWithOrigin {
        @Nonnull final QName name;
        @Nonnull final Origin origin;
        @Nonnull final Documentations docs;
        @Nonnull final LinkedHashMap<QName, AttributeOrAttributeGroup> attributes;
        @Nullable final AnyAttribute anyAttribute;
        @Nonnull
        @Getter(lazy = true)
        private final Set<AttributeGroup> containedGroups
                = this.getAttributes()
                      .values()
                      .stream()
                      .filter(attr -> attr instanceof AttributeGroup)
                      .map(group -> ((AttributeGroup) group))
                      .collect(Collectors.toCollection(Sets::newIdentityHashSet));

        @Override
        public void accept(final NamedConceptWithOriginVisitor visitor) {
            visitor.visit(this);
        }
    }

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static abstract class AttributeDeclaration {
        @Nonnull final QName name;
        @Nonnull final Documentations docs;
        @Nonnull final QName type;

        public abstract Attribute createUse(@Nonnull final Documentations docs, final boolean required,
                                            @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue);
    }

    @ToString
    @Getter
    public static class GlobalAttributeDeclaration extends AttributeDeclaration implements NamedConceptWithOrigin {
        @Nonnull final Origin origin;
        @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue;

        public GlobalAttributeDeclaration(@Nonnull final QName name,
                                          @Nonnull final Origin origin,
                                          @Nonnull final Documentations docs,
                                          @Nonnull final QName type,
                                          @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue) {
            super(name, docs, type);
            this.origin = origin;
            this.defaultOrFixedValue = defaultOrFixedValue;
        }

        @Override
        public GlobalAttribute createUse(@Nonnull final Documentations docs, final boolean required,
                                         @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue) {
            return new GlobalAttribute(name, docs, required, defaultOrFixedValue);
        }

        @Override
        public void accept(final NamedConceptWithOriginVisitor visitor) {
            visitor.visit(this);
        }
    }

    @ToString
    @Getter
    public static class LocalAttributeDeclaration extends AttributeDeclaration {
        public LocalAttributeDeclaration(@Nonnull final QName name,
                                         @Nonnull final Documentations docs,
                                         @Nonnull final QName type) {
            super(name, docs, type);
        }

        @Override
        public LocalAttribute createUse(@Nonnull final Documentations docs, final boolean required,
                                        @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue) {
            return new LocalAttribute(docs, required, this, defaultOrFixedValue);
        }
    }

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static abstract class AttributeOrAttributeGroup implements Visitable<AttributeVisitor> {
        @Nonnull final Documentations docs;
    }

    @ToString
    @Getter
    public static abstract class Attribute extends AttributeOrAttributeGroup {
        final boolean required;
        @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue;

        public Attribute(final Documentations docs, final boolean required,
                         @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue) {
            super(docs);
            this.required = required;
            this.defaultOrFixedValue = defaultOrFixedValue;
        }
    }

    @ToString
    @Getter
    public static class LocalAttribute extends Attribute {
        @Nonnull final LocalAttributeDeclaration localAttributeDeclaration;

        public LocalAttribute(@Nonnull final Documentations docs,
                              final boolean required,
                              @Nonnull final LocalAttributeDeclaration localAttributeDeclaration,
                              @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue) {
            super(docs, required, defaultOrFixedValue);
            this.localAttributeDeclaration = localAttributeDeclaration;
        }

        @Override
        public void accept(final AttributeVisitor visitor) {
            visitor.visit(this);
        }
    }

    @ToString
    @Getter
    public static class GlobalAttribute extends Attribute {
        @Nonnull final QName globalAttributeDeclarationName;

        public GlobalAttribute(@Nonnull final QName globalAttributeDeclarationName,
                               @Nonnull final Documentations docs,
                               final boolean required,
                               @Nonnull final AttributeDefaultOrFixedValue defaultOrFixedValue) {
            super(docs, required, defaultOrFixedValue);
            this.globalAttributeDeclarationName = globalAttributeDeclarationName;
        }

        @Override
        public void accept(final AttributeVisitor visitor) {
            visitor.visit(this);
        }
    }

    @ToString
    @Getter
    public static class AttributeGroup extends AttributeOrAttributeGroup {
        @Nonnull final QName attributeGroupDeclarationName;

        public AttributeGroup(@Nonnull final QName attributeGroupDeclarationName,
                              @Nonnull final Documentations docs) {
            super(docs);
            this.attributeGroupDeclarationName = attributeGroupDeclarationName;
        }

        @Override
        public void accept(final AttributeVisitor visitor) {
            visitor.visit(this);
        }
    }

    @RequiredArgsConstructor
    @ToString
    @Getter
    public static class AnyAttribute {
        @Nonnull final Documentations docs;
        @Nonnull final String processContents;
        @Nonnull final List<String> namespaces;

        public AnyAttribute(@Nonnull final Documentations docs,
                            @Nonnull final String processContents,
                            @Nullable final String[] namespaces) {
            this(docs, processContents, namespaces == null ? Collections.emptyList() : Arrays.asList(namespaces));
        }
    }

    @Nonnull final LinkedHashMap<QName, AttributeOrAttributeGroup> attributes;
    @Nullable final AnyAttribute anyAttribute;
}
