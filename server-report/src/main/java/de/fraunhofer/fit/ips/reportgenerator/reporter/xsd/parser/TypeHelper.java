package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.parser;

import com.google.common.collect.Sets;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Attributes;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Derivation;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Documentations;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Origin;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Type;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeGroupDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSGroupDecl;
import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSValue;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.w3c.dom.TypeInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 * @since 16.01.2018
 */
public class TypeHelper {

    @Getter
    private final Map<XSObject, Type> typeMap = new IdentityHashMap<>();
    @Getter
    private final Map<XSAttributeDeclaration, Attributes.GlobalAttributeDeclaration> attributeMap = new IdentityHashMap<>();
    @Getter
    private final Map<QName, Attributes.GlobalAttributeGroupDeclaration> attributeGroupMap = new HashMap<>();

    @Getter
    private final List<XSElementDecl> topLevelElements = new LinkedList<>();

    private final IdentityHashMap<XSModelGroup, QName> groupMap = new IdentityHashMap<>();
    private final IdentityHashMap<XSParticle, QName> extensionBaseMap = new IdentityHashMap<>();

    private final DocumentationsExtractor documentationsExtractor;

    @Getter
    // (namespace, prefix)
    private final Map<String, String> namespaceToPrefixMap;

    public String getPrefix(final String namespace) {
        return namespaceToPrefixMap.getOrDefault(namespace, "");
    }

    TypeHelper(DocumentationsExtractor documentationsExtractor, String uri, XSNamespaceItemList namespaces,
               Map<String, String> namespaceToPrefixMap) {
        this.documentationsExtractor = documentationsExtractor;
        this.namespaceToPrefixMap = namespaceToPrefixMap;
        for (int i = 0; i < namespaces.getLength(); i++) {
            SchemaGrammar schemaGrammar = (SchemaGrammar) namespaces.item(i);
            if (schemaGrammar instanceof SchemaGrammar.BuiltinSchemaGrammar) {
                process((componentType, processor) -> processBuiltInObjectList(schemaGrammar.getComponents((short) componentType), processor));
            } else {
                process((componentType, processor) -> processNotBuiltInObjectList(uri, schemaGrammar.getComponentsExt((short) componentType), processor));
            }
        }
        // for every top level attribute group, go through their global attributes and make sure that the they are not `inherited` by included groups
        for (final Attributes.GlobalAttributeGroupDeclaration groupDeclaration : attributeGroupMap.values()) {
            groupDeclaration.getAttributes().values().removeIf(attributeOrAttributeGroup ->
                    attributeOrAttributeGroup instanceof Attributes.GlobalAttribute
                            && groupDeclaration.getContainedGroups().stream().anyMatch(cg ->
                            attributeGroupMap.get(cg.getAttributeGroupDeclarationName())
                                             .getAttributes()
                                             .containsKey(
                                                     ((Attributes.GlobalAttribute) attributeOrAttributeGroup).getGlobalAttributeDeclarationName()))
            );
        }
        // for every top level attribute group, go through their attributes and make sure that the groups
        // referenced are not `inherited` by including groups that include further groups themselves
        for (final Attributes.GlobalAttributeGroupDeclaration groupDeclaration : attributeGroupMap.values()) {
            final Set<Attributes.AttributeGroup> inheritedGroups = Sets.newIdentityHashSet();
            for (final Attributes.AttributeGroup containedGroup : groupDeclaration.getContainedGroups()) {
                final QName containedGroupDeclarationName = containedGroup.getAttributeGroupDeclarationName();
                final Attributes.GlobalAttributeGroupDeclaration containedGroupDeclaration = attributeGroupMap.get(containedGroupDeclarationName);
                inheritedGroups.addAll(containedGroupDeclaration.getContainedGroups());
            }
            final LinkedHashMap<QName, Attributes.AttributeOrAttributeGroup> attributes = groupDeclaration.getAttributes();
            inheritedGroups.forEach(group -> attributes.remove(group.getAttributeGroupDeclarationName()));
        }
    }

    private void process(final BiConsumer<Short, BiConsumer<XSObject, Origin>> processObjectList) {
        // process top level element declarations
        processObjectList.accept(
                XSConstants.ELEMENT_DECLARATION,
                (xsObject, origin) -> {
                    Type type = createType(xsObject, origin);
                    final XSElementDecl xsElementDecl;
                    try {
                        xsElementDecl = (XSElementDecl) xsObject;
                    } catch (final ClassCastException e) {
                        throw new RuntimeException("Unexpected XSObject instance: " + xsObject.getClass());
                    }
                    topLevelElements.add(xsElementDecl);
                    if (null == type) {
                        return;
                    }
                    typeMap.put(xsElementDecl.fType, type);
                }
        );

        // process type definitions
        processObjectList.accept(
                XSConstants.TYPE_DEFINITION,
                (xsObject, origin) -> {
                    Type type = createType(xsObject, origin);
                    if (null == type) {
                        return;
                    }
                    typeMap.put(xsObject, type);
                }
        );

        // process group definitions
        processObjectList.accept(
                XSConstants.MODEL_GROUP_DEFINITION,
                (xsObject, origin) -> {
                    Type type = createType(xsObject, origin);
                    if (null == type) {
                        return;
                    }
                    typeMap.put(xsObject, type);
                    XSGroupDecl groupDecl = (XSGroupDecl) xsObject;
                    String prefix = getPrefix(groupDecl.getNamespace());
                    groupMap.put(groupDecl.getModelGroup(), new QName(groupDecl.getNamespace(), groupDecl.getName(), prefix));
                }
        );

        // process top level attribute declarations
        processObjectList.accept(
                XSConstants.ATTRIBUTE_DECLARATION,
                (xsObject, origin) -> {
                    final XSAttributeDecl xsAttributeDecl;
                    try {
                        xsAttributeDecl = (XSAttributeDecl) xsObject;
                    } catch (final ClassCastException e) {
                        throw new RuntimeException("Unexpected XSObject instance: " + xsObject.getClass());
                    }
                    getOrCreateAttributeDeclaration(xsAttributeDecl, origin);
                }
        );

        // process top level attribute group declarations
        processObjectList.accept(
                XSConstants.ATTRIBUTE_GROUP,
                (xsObject, origin) -> {
                    final XSAttributeGroupDecl xsAttributeGroupDecl;
                    try {
                        xsAttributeGroupDecl = (XSAttributeGroupDecl) xsObject;
                    } catch (final ClassCastException e) {
                        throw new RuntimeException("Unexpected XSObject instance: " + xsObject.getClass());
                    }
                    createAttributeGroupDeclaration(xsObject, origin, xsAttributeGroupDecl);
                }
        );
    }

    private void processBuiltInObjectList(final XSNamedMap objectList, final BiConsumer<XSObject, Origin> processor) {
        for (final Object o : objectList.values()) {
            if (!(o instanceof XSObject)) {
                throw new RuntimeException("Unexpected object types");
            }
            XSObject xsObject = (XSObject) o;
            processor.accept(xsObject, Origin.XML_SCHEMA_XSD);
        }
    }

    private void processNotBuiltInObjectList(final String uri, final ObjectList objectList,
                                             final BiConsumer<XSObject, Origin> processor) {
        for (int i = 0, length = objectList.getLength() - 1; i < length; i = i + 2) {
            Object file = objectList.get(i);
            Object data = objectList.get(i + 1);

            if (!(file instanceof String) || !(data instanceof XSObject)) {
                throw new RuntimeException("Unexpected object types");
            }

            // Example: file:///Users/sgokay/git/omp-tool/server/report-generator/src/test/resources/xsd/IBIS-IP_common_V1.0.xsd,ServiceStartListStructure
            final String fileString = (String) file;
            // since xsd type names may not contain ',', we can split the string at the last ',' occurrence
            final String path = fileString.substring(0, fileString.lastIndexOf(","));

            XSObject xsObject = (XSObject) data;
            boolean isInternal = path.contains(uri);

            Origin origin = new Origin(isInternal, path);
            processor.accept(xsObject, origin);
        }
    }

    private static @Nonnull
    Attributes.AttributeDefaultOrFixedValue defaultOrFixedValue(final java.util.function.IntSupplier getConstraintType,
                                                                final Supplier<XSValue> getValueConstraintValue) {
        switch (getConstraintType.getAsInt()) {
            case XSConstants.VC_FIXED:
                return Attributes.AttributeDefaultOrFixedValue.createFixed(getValueConstraintValue.get().getNormalizedValue());
            case XSConstants.VC_DEFAULT:
                return Attributes.AttributeDefaultOrFixedValue.createDefault(getValueConstraintValue.get().getNormalizedValue());
            case XSConstants.VC_NONE:
                return Attributes.AttributeDefaultOrFixedValue.NONE;
        }
        throw new IllegalArgumentException("unknown value constraint");
    }

    public static @Nonnull
    Attributes.AttributeDefaultOrFixedValue defaultOrFixedValue(final XSAttributeUse attributeUse) {
        return defaultOrFixedValue(attributeUse::getConstraintType, attributeUse::getValueConstraintValue);
    }

    public static @Nonnull
    Attributes.AttributeDefaultOrFixedValue defaultOrFixedValue(final XSAttributeDeclaration attributeDeclaration) {
        return defaultOrFixedValue(attributeDeclaration::getConstraintType, attributeDeclaration::getValueConstraintValue);
    }


    private void createAttributeGroupDeclaration(final XSObject xsObject,
                                                 final Origin origin,
                                                 final XSAttributeGroupDecl xsAttributeGroupDecl) {
        final QName groupName = buildQName(xsObject);
        final Documentations groupDocs = documentationsExtractor.fromAnnotations(xsAttributeGroupDecl.getAnnotations());

        final XSObjectList attributeUses = xsAttributeGroupDecl.getAttributeUses();
        final LinkedHashMap<QName, Attributes.AttributeOrAttributeGroup> attributes = new LinkedHashMap<>();

        final Set<XSObject> groupsAlreadySeen = Sets.newIdentityHashSet();

        for (final Object o : attributeUses) {
            final XSAttributeUse attributeUse = ((XSAttributeUse) o);
            final Documentations useDocs = documentationsExtractor.fromAnnotations(attributeUse.getAnnotations());
            final XSAttributeDeclaration xsAttributeDeclaration = attributeUse.getAttrDeclaration();
            final XSObject xsAttributeDeclarationParent = xsAttributeDeclaration.getParent();
            // if parent is null, then the attribute is a ref to global attribute
            // if parent is not the current group, then the attribute is here because of a group ref
            if (null != xsAttributeDeclarationParent && xsAttributeDeclarationParent != xsAttributeGroupDecl) {
                // if we don't already have the group in our list, add it
                if (groupsAlreadySeen.add(xsAttributeDeclarationParent)) {
                    if (XSConstants.ATTRIBUTE_GROUP != xsAttributeDeclarationParent.getType()) {
                        throw new IllegalStateException("Parent of attribute inside of attribute group is not an attribute group!");
                    }
                    final QName parentGroupName = buildQName(xsAttributeDeclarationParent);
                    attributes.put(parentGroupName, new Attributes.AttributeGroup(parentGroupName, useDocs));
                }
                continue;
            }
            final String xsAttributeDeclNS = xsAttributeDeclaration.getNamespace();
            final boolean internal = origin.isInternal() && (xsAttributeDeclNS == null || Objects.equals(xsAttributeGroupDecl.getNamespace(), xsAttributeDeclNS));
            final Attributes.AttributeDeclaration attributeDeclaration = getOrCreateAttributeDeclaration(xsAttributeDeclaration, internal ? origin : new Origin(false, ""));

            attributes.put(attributeDeclaration.getName(), attributeDeclaration.createUse(useDocs, attributeUse.getRequired(), defaultOrFixedValue(attributeUse)));
        }

        final XSWildcardDecl xsWildcard = xsAttributeGroupDecl.fAttributeWC;
        final Attributes.AnyAttribute anyAttribute = xsWildcard == null ? null : new Attributes.AnyAttribute(
                documentationsExtractor.fromAnnotations(xsWildcard.getAnnotations()),
                xsWildcard.getProcessContentsAsString(),
                xsWildcard.fNamespaceList
        );

        attributeGroupMap.put(groupName, new Attributes.GlobalAttributeGroupDeclaration(groupName, origin, groupDocs, attributes, anyAttribute));
    }

    public Attributes.AttributeDeclaration getOrCreateAttributeDeclaration(
            final XSAttributeDeclaration xsAttributeDecl,
            final Origin origin) {
        final boolean topLevelAttribute = null == xsAttributeDecl.getParent();
        // top level attributes may occur several times
        if (topLevelAttribute) {
            final Attributes.AttributeDeclaration previous = attributeMap.get(xsAttributeDecl);
            if (null != previous) {
                return previous;
            }
        }

        final QName attributeName = buildQName(xsAttributeDecl);
        final Documentations docs = documentationsExtractor.fromAnnotations(xsAttributeDecl.getAnnotations());

        final QName typeName;
        final XSSimpleTypeDefinition typeDefinition = xsAttributeDecl.getTypeDefinition();
        if (typeDefinition.getAnonymous()) {
            final Type anonymousType = typeMap.computeIfAbsent(typeDefinition,
                    b -> createAnonymousType(typeDefinition, origin, xsAttributeDecl.getNamespace())
            );
            assert anonymousType != null;
            typeName = anonymousType.getName();
        } else {
            typeName = buildQName(typeDefinition);
        }


        // only store top-level elements
        if (!topLevelAttribute) {
            return new Attributes.LocalAttributeDeclaration(attributeName, docs, typeName);
        }

        final Attributes.AttributeDefaultOrFixedValue defaultOrFixedValue = defaultOrFixedValue(xsAttributeDecl);
        final Attributes.GlobalAttributeDeclaration globalAttributeDeclaration = new Attributes.GlobalAttributeDeclaration(attributeName, origin, docs, typeName, defaultOrFixedValue);
        attributeMap.put(xsAttributeDecl, globalAttributeDeclaration);
        return globalAttributeDeclaration;
    }

    public Origin findOrigin(XSObject xsObject) {
        Type type = typeMap.get(xsObject);
        if (type != null) {
            return type.getOrigin();
        }

        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(xsObject.getNamespace())) {
            return Origin.XML_SCHEMA_XSD;
        }

        throw new RuntimeException("Unexpected things happened: " + xsObject);
    }

    @Nullable
    public QName findExtension(XSParticle particle) {
        return extensionBaseMap.get(particle);
    }

    @Nullable
    public boolean containedInExtensionBaseMap(XSParticle particle) {
        return extensionBaseMap.containsKey(particle);
    }

    public QName findQNameForTypeDefinition(XSObject xsObject) {
        Type type = typeMap.get(xsObject);
        if (type != null) {
            return type.getName();
        }

        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(xsObject.getNamespace())) {
            return new QName(xsObject.getNamespace(), xsObject.getName(), getPrefix(xsObject.getNamespace()));
        }

        throw new RuntimeException("Unexpected things happened: " + xsObject);
    }

    @Nullable
    public QName findGroupName(XSModelGroup group) {
        return groupMap.get(group);
    }

    @Nullable
    private Type createType(XSObject xsObject, Origin origin) {
        return createType(xsObject, buildQName(xsObject), origin);
    }

    @Nullable
    private Type createType(XSObject xsObject, QName qName, Origin origin) {
        if (xsObject instanceof XSSimpleTypeDecl) {
            return createSimpleType((XSSimpleTypeDecl) xsObject, qName, origin);
        } else if (xsObject instanceof XSComplexTypeDecl) {
            return createComplexType((XSComplexTypeDecl) xsObject, qName, origin);
        } else if (xsObject instanceof XSGroupDecl) {
            return createGroup((XSGroupDecl) xsObject, qName, origin);
        } else if (xsObject instanceof XSElementDecl) {
            return createAnonymousTypeFromElement((XSElementDecl) xsObject, origin, xsObject.getNamespace());
        } else {
            throw new RuntimeException("Unexpected XSObject instance: " + xsObject.getClass());
        }
    }

    @Nullable
    private Type createAnonymousTypeFromElement(final XSElementDecl element, final Origin elementOrigin,
                                                final String internalNamespace) {
        final XSTypeDefinition typeDefinition = element.getTypeDefinition();
        if (!typeDefinition.getAnonymous()) {
            return null;
        }
        return createAnonymousType(typeDefinition, elementOrigin, internalNamespace);
    }

    @Nullable
    public Type createAnonymousType(final XSTypeDefinition typeDefinition, final Origin elementOrigin,
                                    final String internalNamespace) {
        if (typeMap.containsKey(typeDefinition)) {
            return null;
        }
        String name = typeDefinition.getName();
        if (null == name && typeDefinition instanceof TypeInfo) {
            name = ((TypeInfo) typeDefinition).getTypeName();
        }
        if (null == name) {
            throw new IllegalArgumentException("cannot determine name of type!");
        }
        final boolean isInternal = elementOrigin.isInternal() && Objects.equals(typeDefinition.getNamespace(), internalNamespace);
        final Origin typeOrigin = new Origin(isInternal, true, elementOrigin.getXsdPath());
        final QName qName = new QName(typeDefinition.getNamespace(), name, getPrefix(typeDefinition.getNamespace()));
        return createType(typeDefinition, qName, typeOrigin);
    }

    private Type createGroup(XSGroupDecl group, QName qName, Origin origin) {
        Documentations docs = documentationsExtractor.fromAnnotations(group.getAnnotations());
        return new Type.Group(qName, origin, docs);
    }

    private Type.Simple createSimpleType(XSSimpleTypeDecl simple, QName qName, Origin origin) {
        Documentations docs = documentationsExtractor.fromAnnotations(simple.getAnnotations());
        switch (simple.getVariety()) {
            case XSSimpleTypeDefinition.VARIETY_LIST:
                return new Type.Simple.List(qName, origin, docs);
            case XSSimpleTypeDefinition.VARIETY_UNION:
                return new Type.Simple.Union(qName, origin, docs);
            default: {
                final List<Type.Simple.Enumeration.Value> lexicalEnumeration = getEnumerationValues(simple);
                final XSTypeDefinition baseType = simple.getBaseType();
                final QName baseName = null == baseType ? null : buildQName(baseType);
                if (lexicalEnumeration.isEmpty()) {
                    // simple restriction type
                    return new Type.Simple.Restriction(qName, origin, docs, baseName, extractMinMax(simple));
                } else {
                    // enumeration type
                    return new Type.Simple.Enumeration(qName, origin, docs, baseName, lexicalEnumeration);
                }
            }
        }
    }

    private List<Type.Simple.Enumeration.Value> getEnumerationValues(XSSimpleTypeDecl simple) {
        final StringList lexicalEnumeration = simple.getLexicalEnumeration();
        final int numValues = lexicalEnumeration.size();
        if (0 == numValues) {
            return Collections.emptyList();
        }
        final XSObjectList enumerationAnnotations = simple.enumerationAnnotations;

        // Actually Xerces initializes the annotation of an 'enum value without annotation' with a null Object, such
        // that the sizes of both lists and corresponding indices match. But, let's be defensive anyway.
        if (numValues != enumerationAnnotations.size()) {
            throw new IllegalArgumentException("The size of enum values and their annotations do not match");
        }

        final ArrayList<Type.Simple.Enumeration.Value> values = new ArrayList<>();
        for (int i = 0; i < numValues; ++i) {
            values.add(new Type.Simple.Enumeration.Value(
                    documentationsExtractor.fromAnnotation(enumerationAnnotations.item(i)),
                    lexicalEnumeration.item(i)
            ));
        }
        return values;
    }

    private Type.Complex createComplexType(XSComplexTypeDecl complex, QName qName, Origin origin) {
        Documentations docs = documentationsExtractor.fromAnnotations(complex.getAnnotations());
        Derivation derivation = Derivation.newInstance(complex, base -> {
            final String namespace = base.getNamespace();
            return new QName(namespace, base.getName(), getPrefix(namespace));
        });
        if (derivation.getType() == Derivation.Type.EXTENSION) {
            final XSTypeDefinition baseType = complex.getBaseType();
            if (baseType instanceof XSComplexTypeDecl) {
                XSComplexTypeDecl complexBaseType = (XSComplexTypeDecl) baseType;
                XSParticle particle = complexBaseType.getParticle();
                extensionBaseMap.put(particle, derivation.getBaseName());
            } else if (baseType instanceof XSSimpleTypeDecl) {
                // FIXME think about what we don't want to print in child types?
                final XSSimpleTypeDecl simpleBaseType = (XSSimpleTypeDecl) baseType;

            }
        }
        return new Type.Complex(qName, origin, derivation, docs);
    }

    public QName buildQName(XSObject xsObject) {
        String prefix = getPrefix(xsObject.getNamespace());
        return new QName(xsObject.getNamespace(), xsObject.getName(), prefix);
    }

    @Nullable
    private static String extractMinMax(XSSimpleTypeDecl simple) {
        Pair<Object, IncExc> min = decideIncExc(simple.getMinInclusiveValue(), simple.getMinExclusiveValue());
        Pair<Object, IncExc> max = decideIncExc(simple.getMaxInclusiveValue(), simple.getMaxExclusiveValue());

        if (min == null && max == null) {
            return null;
        }

        String minString = "(*";
        String maxString = "*)";

        if (min != null) {
            minString = min.getRight().startCode + min.getLeft().toString();
        }

        if (max != null) {
            maxString = max.getLeft().toString() + max.getRight().endCode;
        }

        return minString + ", " + maxString;
    }

    @Nullable
    private static Pair<Object, IncExc> decideIncExc(Object inclusive, Object exclusive) {
        if (inclusive == null) {
            if (exclusive == null) {
                return null;
            } else {
                return Pair.of(exclusive, IncExc.EXC);
            }
        } else {
            if (exclusive == null) {
                return Pair.of(inclusive, IncExc.INC);
            } else {
                // In this case Xerces already indicates an error, so we can just ignore it.
                return null;
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum IncExc {
        INC("[", "]"),
        EXC("(", ")");

        private final String startCode;
        private final String endCode;
    }
}
