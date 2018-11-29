package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.parser;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.fraunhofer.fit.ips.reportgenerator.exception.RuntimeXsdException;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Attributes;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Choice;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Derivation;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Documentations;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Element;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.ElementList;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.GroupRef;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Origin;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Sequence;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.SequenceOrChoice;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.SequenceOrChoiceOrGroupRef;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.SequenceOrChoiceOrGroupRefOrElementList;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Type;
import de.fraunhofer.fit.ips.reportgenerator.reporter.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSAttributeGroupDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSGroupDecl;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.xerces.impl.xs.XSParticleDecl.PARTICLE_ELEMENT;
import static org.apache.xerces.impl.xs.XSParticleDecl.PARTICLE_MODELGROUP;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 * @since 13.12.2017
 */
@Slf4j
public class XSDParser {

    private final DocumentationsExtractor documentationsExtractor;
    private final String uri;
    private final TypeHelper typeHelper;
    private final Map<QName, Type.Simple> simpleTypes = new HashMap<>();
    private final Map<QName, Type.Complex> complexTypes = new HashMap<>();
    private final Map<QName, Type.Group> groupTypes = new HashMap<>();

    public static void main(String[] args) {
//        XSDParser xsdParser = XSDParser.createFromData(new String(
//                Files.readAllBytes(
//                        Paths.get(XSDParser.class
//                                .getClassLoader()
//                                .getResource("xsd/IBIS-IP_PassengerCountingService_V1.0.xsd")
//                                .toURI())
//                ), StandardCharsets.UTF_8.name()
//        ));

        XSDParser xsdParser = XSDParser.createFromUri("src/test/resources/xsd/asd.xsd");
        xsdParser.process();
    }

    public static XSDParser createFromUri(String uri) {
        return new XSDParser(uri, Utils.createSchemaLoader().loadURI(uri), PrefixExtractor.fromUri(uri));
    }

    public static XSDParser createFromData(final String data) {
        final String systemIdAsUri = UUID.randomUUID().toString();
        final DOMInputImpl is = new DOMInputImpl(null, systemIdAsUri, null, new StringReader(data), Charsets.UTF_8.toString());
        return new XSDParser(systemIdAsUri, Utils.createSchemaLoader().load(is), PrefixExtractor.fromData(data));
    }

    public Schema process() {
        ImmutableMap.copyOf(typeHelper.getTypeMap()).forEach(this::processType);
        final Map<QName, Element> topLevelElements = processTopLevelElements();
        final ImmutableMap<QName, NamedConceptWithOrigin> concepts = ImmutableMap.<QName, NamedConceptWithOrigin>builder()
                .putAll(simpleTypes)
                .putAll(complexTypes)
                .putAll(groupTypes)
                .putAll(topLevelElements)
                .putAll(Maps.uniqueIndex(typeHelper.getAttributeMap().values(), Attributes.AttributeDeclaration::getName))
                .putAll(typeHelper.getAttributeGroupMap())
                .build();
        final String internalNamespace = concepts.values()
                                                 .stream()
                                                 .filter(c -> c.getOrigin().isInternal())
                                                 .findAny()
                                                 .map(c -> c.getName().getNamespaceURI())
                                                 .orElse("");
        final Map<String, String> namespaceToPrefixMap = typeHelper.getNamespaceToPrefixMap();
        final String internalPrefix = namespaceToPrefixMap.getOrDefault(internalNamespace, "");
        if ("".equals(internalPrefix)) {
            namespaceToPrefixMap.put(internalNamespace, Config.LOCAL_PREFIX_IF_MISSING);
        }
        return new Schema(uri, namespaceToPrefixMap, concepts);
    }

    private Map<QName, Element> processTopLevelElements() {
        return typeHelper.getTopLevelElements().stream().map(xsElementDecl -> {
            final Documentations doc = documentationsExtractor.fromAnnotations(xsElementDecl.getAnnotations());
            final XSTypeDefinition typeDefinition = xsElementDecl.getTypeDefinition();
            final QName typeQName = typeHelper.findQNameForTypeDefinition(typeDefinition);
            final Origin origin = typeHelper.findOrigin(typeDefinition);
            final QName elQName = typeHelper.buildQName(xsElementDecl);
            // cardinality makes no sense here, minOccurs maxOccurs is not allowed on top level elements
            final Element sp = new Element(elQName, typeQName, getCardinality(0, 1), origin, doc);
            log.debug("name={} type={} doc={}", sp.getName(), sp.getDataType(), sp.getDocs());
            return sp;
        }).collect(Collectors.toMap(Element::getName, java.util.function.Function.identity()));
    }

    private XSDParser(String uri, XSModel xsModel, Map<String, String> namespaceToPrefixMap) {
        if (xsModel == null) {
            throw new RuntimeXsdException("XSModel is null (DOM impl could not parse the string)");
        }
        documentationsExtractor = new DocumentationsExtractor();
        this.uri = uri;
        namespaceToPrefixMap.put(XMLConstants.W3C_XML_SCHEMA_NS_URI, Config.XSD_PREFIX);
        typeHelper = new TypeHelper(documentationsExtractor, uri, xsModel.getNamespaceItems(), namespaceToPrefixMap);
    }

    private void processType(XSObject xsObject, Type type) {
        if (type instanceof Type.Simple) {
            processSimpleType((XSSimpleTypeDecl) xsObject, (Type.Simple) type);
        } else if (type instanceof Type.Group) {
            processGroup((XSGroupDecl) xsObject, (Type.Group) type);
        } else if (type instanceof Type.Complex) {
            processComplexType((XSComplexTypeDecl) xsObject, (Type.Complex) type);
        } else {
            log.warn("Unexpected Type instance: {}", type.getClass());
        }
    }

    private void processSimpleType(XSSimpleTypeDecl simple, Type.Simple simpleType) {
        Type.Simple previous = simpleTypes.putIfAbsent(simpleType.getName(), simpleType);
        checkForNull(simpleType.getName(), previous);

        log.debug("name={} type={} doc={}", simpleType.getName(), simple.getClass(), simpleType.getDocs());
    }

    private LinkedHashMap<QName, Attributes.AttributeOrAttributeGroup> createAttributeList(
            final XSObjectList ours, final Origin origin) {
        return createAttributeListForSlimExtensions(ours, XSObjectListImpl.EMPTY_LIST, origin);
    }

    private LinkedHashMap<QName, Attributes.AttributeOrAttributeGroup> createAttributeListForSlimExtensions(
            final XSObjectList ours, final XSObjectList base, final Origin origin) {
        final Set<XSAttributeDeclaration> baseAD = Sets.newIdentityHashSet();
        for (final Object o : base) {
            final XSAttributeUse xsAttributeUse = (XSAttributeUse) o;
            baseAD.add(xsAttributeUse.getAttrDeclaration());
        }
        final IdentityHashMap<XSAttributeGroupDecl, Attributes.AttributeGroup> groups = new IdentityHashMap<>();
        final LinkedHashMap<QName, Attributes.AttributeOrAttributeGroup> attributes = new LinkedHashMap<>();
        for (final Object o : ours) {
            final XSAttributeUse xsAttributeUse = (XSAttributeUse) o;
            final XSAttributeDeclaration xsAttributeDeclaration = xsAttributeUse.getAttrDeclaration();
            if (baseAD.contains(xsAttributeDeclaration)) {
                continue;
            }
            @Nullable final XSObject xsAttributeDeclarationParent = xsAttributeDeclaration.getParent();
            if (xsAttributeDeclarationParent instanceof XSAttributeGroupDecl) {
                final XSAttributeGroupDecl xsAttributeGroupDeclaration = (XSAttributeGroupDecl) xsAttributeDeclarationParent;
                groups.computeIfAbsent(xsAttributeGroupDeclaration, gd -> {
                    final Attributes.AttributeGroup attributeGroup = new Attributes.AttributeGroup(typeHelper.buildQName(gd), documentationsExtractor.fromAnnotations(gd.getAnnotations()));
                    attributes.put(attributeGroup.getAttributeGroupDeclarationName(), attributeGroup);
                    return attributeGroup;
                });
            } else {
                final Attributes.AttributeDeclaration attributeDeclaration = typeHelper.getOrCreateAttributeDeclaration(xsAttributeDeclaration, origin);
                final Attributes.Attribute attribute = attributeDeclaration.createUse(documentationsExtractor.fromAnnotations(xsAttributeUse.getAnnotations()), xsAttributeUse.getRequired(), TypeHelper.defaultOrFixedValue(xsAttributeUse));
                attributes.put(attributeDeclaration.getName(), attribute);
            }
        }
        final Map<QName, Attributes.GlobalAttributeGroupDeclaration> attributeGroupMap = typeHelper.getAttributeGroupMap();
        // go through the global attributes and make sure that they are not `inherited` by included groups
        attributes.values().removeIf(attributeOrAttributeGroup ->
                attributeOrAttributeGroup instanceof Attributes.GlobalAttribute
                        && groups.values().stream().anyMatch(group ->
                        attributeGroupMap.get(group.getAttributeGroupDeclarationName())
                                         .getAttributes()
                                         .containsKey(
                                                 ((Attributes.GlobalAttribute) attributeOrAttributeGroup).getGlobalAttributeDeclarationName()))
        );
        // go through the attributes and make sure that the groups referenced are not `inherited` by including groups that include further groups themselves
        for (final Attributes.AttributeGroup directGroup : groups.values()) {
            for (Attributes.AttributeGroup indirectGroup : attributeGroupMap.get(directGroup.getAttributeGroupDeclarationName()).getContainedGroups()) {
                attributes.remove(indirectGroup.getAttributeGroupDeclarationName());
            }
        }
        return attributes;
    }

    private void processComplexType(XSComplexTypeDecl complex, Type.Complex complexType) {
        final QName complexTypeName = complexType.getName();
        Type.Complex previous = complexTypes.putIfAbsent(complexTypeName, complexType);
        checkForNull(complexTypeName, previous);

        log.debug("name={} doc={}", complexTypeName, complexType.getDocs());

        final boolean derivedByExtension = complexType.getDerivation().getType() == Derivation.Type.EXTENSION;

        final XSWildcardDecl wildcard;
        final LinkedHashMap<QName, Attributes.AttributeOrAttributeGroup> attributes;

        final XSAttributeGroupDecl ourAttrGroup = complex.getAttrGrp();
        final XSWildcardDecl ourAttributeWildcard = ourAttrGroup.fAttributeWC;
        final XSObjectList ourAttributeUses = ourAttrGroup.getAttributeUses();
        if (derivedByExtension && complex.getBaseType() instanceof XSComplexTypeDecl) {
            // identify the added attributes
            final XSComplexTypeDecl base = (XSComplexTypeDecl) complex.getBaseType();
            final XSAttributeGroupDecl baseAttrGrp = base.getAttrGrp();
            final XSObjectList baseAttributeUses = baseAttrGrp.getAttributeUses();
            attributes = createAttributeListForSlimExtensions(ourAttributeUses, baseAttributeUses, complexType.getOrigin());
            final XSWildcardDecl baseAttributeWildcard = baseAttrGrp.fAttributeWC;
            wildcard = baseAttributeWildcard == ourAttributeWildcard ? null : ourAttributeWildcard;
        } else {
            attributes = createAttributeList(ourAttributeUses, complexType.getOrigin());
            wildcard = ourAttributeWildcard;
        }
        complexType.setAttributes(new Attributes(attributes,
                null == wildcard ? null :
                        new Attributes.AnyAttribute(
                                documentationsExtractor.fromAnnotations(wildcard.getAnnotations()),
                                wildcard.getProcessContentsAsString(),
                                wildcard.fNamespaceList
                        )));

        XSParticleDecl xsParticle = (XSParticleDecl) complex.getParticle();
        if (xsParticle == null) {
            // ComplexType does not have children yet
            complexType.setParticle(visitor -> {
                // No-op
            });
        } else {
            if (derivedByExtension) {
                final QName extensionName = typeHelper.findExtension(xsParticle);
                // if extensionName equals complexTypeName, then I found my own particle added by one of my children
                // thus, I only discard the whole particle if it belongs to a type other than myself
                final boolean isBorrowedFromParent = null != extensionName && !extensionName.equals(complexTypeName);
                if (isBorrowedFromParent) {
                    complexType.setParticle(new Sequence(Documentations.EMPTY, Collections.emptyList()));
                    return;
                }
                // elements might belong to parent, Xerces2J might have added wrapper SEQUENCE, which we
                // want to strip away together with the borrowed stuff
                if (PARTICLE_MODELGROUP == xsParticle.fType) {
                    final XSParticleDecl[] children = ((XSModelGroupImpl) xsParticle.fValue).fParticles;
                    if (children.length == 2 && typeHelper.containedInExtensionBaseMap(children[0])) {
                        xsParticle = children[1];
                    }
                }
            }
            switch (xsParticle.fType) {
                case PARTICLE_ELEMENT: {
                    final XSElementDecl fValue = ((XSElementDecl) xsParticle.fValue);

                    break;
                }
                case PARTICLE_MODELGROUP: {
                    XSModelGroupImpl modelGroup = (XSModelGroupImpl) xsParticle.fValue;
                    SequenceOrChoiceOrGroupRef particle = processSequenceOrChoiceOrGroup(modelGroup, complexType);
                    complexType.setParticle(particle);
                    break;
                }
                default:
                    final XSTerm fValue = xsParticle.fValue;
                    break;
            }
        }
    }

    private void processGroup(XSGroupDecl group, Type.Group groupType) {
        Type.Group previous = groupTypes.putIfAbsent(groupType.getName(), groupType);
        checkForNull(groupType.getName(), previous);

        log.debug("name={} doc={}", groupType.getName(), groupType.getDocs());
        XSModelGroupImpl modelGroup = (XSModelGroupImpl) group.getModelGroup();

        SequenceOrChoice particle = processSequenceOrChoice(modelGroup, groupType);
        groupType.setParticle(particle);
    }

    private Element processElement(XSParticleDecl particle, Type mother) {
        final int min = particle.minEffectiveTotalRange();
        final int max = particle.maxEffectiveTotalRange();
        final XSElementDeclaration el = (XSElementDeclaration) particle.fValue;
        final Documentations doc = documentationsExtractor.fromAnnotations(particle.getAnnotations());

        final XSTypeDefinition typeDefinition = el.getTypeDefinition();

        final QName typeQName;
        final Origin origin;
        if (typeDefinition.getAnonymous()) {
            final Type type = typeHelper.getTypeMap().computeIfAbsent(typeDefinition,
                    ignored -> {
                        final Type anonymousType = typeHelper.createAnonymousType(typeDefinition, mother.getOrigin(), mother.getName().getNamespaceURI());
                        if (null == anonymousType) {
                            throw new IllegalArgumentException("could not create anonymous type!");
                        }
                        processType(typeDefinition, anonymousType);
                        return anonymousType;
                    }
            );
            typeQName = type.getName();
            origin = type.getOrigin();
        } else {
            typeQName = typeHelper.findQNameForTypeDefinition(typeDefinition);
            origin = typeHelper.findOrigin(typeDefinition);
        }

        final QName elQName = typeHelper.buildQName(el);
        final Element sp = new Element(elQName, typeQName, getCardinality(min, max), origin, doc);
        log.debug("card={} name={} type={} doc={}", sp.getCardinality(), sp.getName(), sp.getDataType(), sp.getDocs());
        return sp;
    }

    private Element processWildcard(XSParticleDecl particle, Type mother) {
        final int min = particle.minEffectiveTotalRange();
        final int max = particle.maxEffectiveTotalRange();
        final Documentations doc = documentationsExtractor.fromAnnotations(particle.getAnnotations());

        final QName elQName = new QName(SchemaSymbols.URI_SCHEMAFORSCHEMA, SchemaSymbols.ELT_ANY);
        final Element sp = new Element(elQName, elQName, getCardinality(min, max), Origin.XML_SCHEMA_XSD, doc);
        log.debug("card={} name={} type={} doc={}", sp.getCardinality(), sp.getName(), sp.getDataType(), sp.getDocs());
        return sp;
    }

    private SequenceOrChoice processSequenceOrChoice(XSModelGroupImpl modelGroup, Type mother) {
        Documentations docs = documentationsExtractor.fromAnnotations(modelGroup.getAnnotations());

        if (modelGroup.getCompositor() == XSModelGroup.COMPOSITOR_CHOICE) {
            String cardinality = getCardinality(modelGroup);

            List<SequenceOrChoiceOrGroupRefOrElementList> particles = processSequenceOrChoiceOrGroupOrElement(modelGroup, mother);
            return new Choice(cardinality, docs, particles);

        } else if (modelGroup.getCompositor() == XSModelGroup.COMPOSITOR_SEQUENCE) {
            List<SequenceOrChoiceOrGroupRefOrElementList> particles = processSequenceOrChoiceOrGroupOrElement(modelGroup, mother);
            return new Sequence(docs, particles);

        } else if (modelGroup.getCompositor() == XSModelGroup.COMPOSITOR_ALL) {
            throw new RuntimeException("XSModelGroup compositor ALL not yet implemented");

        } else {
            throw new RuntimeException("Unexpected XSModelGroup compositor");
        }
    }

    private SequenceOrChoiceOrGroupRef processSequenceOrChoiceOrGroup(XSModelGroupImpl modelGroup, Type mother) {
        QName groupQName = typeHelper.findGroupName(modelGroup);
        if (groupQName != null) {
            Documentations docs = documentationsExtractor.fromAnnotations(modelGroup.getAnnotations());
            String cardinality = getCardinality(modelGroup);
            return new GroupRef(groupQName, cardinality, docs);
        }

        return processSequenceOrChoice(modelGroup, mother);
    }

    private List<SequenceOrChoiceOrGroupRefOrElementList> processSequenceOrChoiceOrGroupOrElement(
            XSModelGroupImpl modelGroup, Type mother) {
        List<SequenceOrChoiceOrGroupRefOrElementList> particleList = new ArrayList<>();

        if (null == modelGroup.fParticles) {
            return particleList;
        }

        List<Element> elementList = new ArrayList<>();

        for (XSParticleDecl fParticle : modelGroup.fParticles) {
            XSTerm term = fParticle.fValue;
            if (term == null) {
                // TODO: do nothing really?
                continue;
            }

            if (term instanceof XSModelGroupImpl) {
                SequenceOrChoiceOrGroupRef cp = processSequenceOrChoiceOrGroup((XSModelGroupImpl) term, mother);
                addElementListToParticles(particleList, elementList);
                particleList.add(cp);
                elementList = new ArrayList<>();

            } else if (term instanceof XSElementDeclaration) {
                Element el = processElement(fParticle, mother);
                elementList.add(el);

            } else if (term instanceof XSWildcardDecl) {
                Element el = processWildcard(fParticle, mother);
                elementList.add(el);

            } else {
                log.warn("Unexpected XSParticleDecl type: {}", term.getClass());
            }
        }

        addElementListToParticles(particleList, elementList);
        return particleList;
    }

    private static void addElementListToParticles(List<SequenceOrChoiceOrGroupRefOrElementList> particleList,
                                                  List<Element> elementList) {
        if (!elementList.isEmpty()) {
            particleList.add(new ElementList(elementList));
        }
    }

    private static String getCardinality(XSModelGroupImpl modelGroup) {
        int min = modelGroup.minEffectiveTotalRange();
        int max = modelGroup.maxEffectiveTotalRange();
        return getCardinality(min, max);
    }

    private static String getCardinality(int min, int max) {
        String maxStr;
        if (max == -1) {
            // maxOccurs="unbounded"
            maxStr = "*";
        } else {
            maxStr = String.valueOf(max);
        }
        return min + ":" + maxStr;
    }

    private static void checkForNull(QName key, Object obj) {
        if (obj != null) {
            log.warn("there is already a map item with the same key {}", key);
        }
    }
}
