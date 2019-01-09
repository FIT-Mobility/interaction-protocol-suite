package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.xsd.AttributeVisitor;
import de.fraunhofer.fit.ips.model.xsd.Attributes;
import de.fraunhofer.fit.ips.model.xsd.Choice;
import de.fraunhofer.fit.ips.model.xsd.Element;
import de.fraunhofer.fit.ips.model.xsd.ElementList;
import de.fraunhofer.fit.ips.model.xsd.GroupRef;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOriginVisitor;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import de.fraunhofer.fit.ips.model.xsd.Sequence;
import de.fraunhofer.fit.ips.model.xsd.SequenceOrChoiceOrGroupRefOrElementList;
import de.fraunhofer.fit.ips.model.xsd.SequenceOrChoiceOrGroupRefOrElementListVisitor;
import de.fraunhofer.fit.ips.model.xsd.Type;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.xml.namespace.QName;
import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DependenciesVisitor implements SequenceOrChoiceOrGroupRefOrElementListVisitor, NamedConceptWithOriginVisitor, AttributeVisitor {
    private final Schema schema;
    final EnumSet<ParticleType> consideredParticleTypes;
    final Consumer<QName> danglingParticlesConsumer;

    private void consume(final ParticleType particleType, final QName name) {
        if (consideredParticleTypes.contains(particleType)) {
            danglingParticlesConsumer.accept(name);
        }
    }

    @Override
    public void visit(final Sequence sequence) {
        for (final SequenceOrChoiceOrGroupRefOrElementList child : sequence.getParticleList()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(final Choice choice) {
        for (final SequenceOrChoiceOrGroupRefOrElementList child : choice.getParticleList()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(final GroupRef groupRef) {
        final QName refName = groupRef.getRefName();
        final NamedConceptWithOrigin group = schema.getConcept(refName);
        if (null == group || !group.getOrigin().isInternal()) {
            return;
        }
        group.accept(this);
    }

    @Override
    public void visit(final ElementList elementList) {
        for (final Element element : elementList.getElements()) {
            final QName childName = element.getDataType();
            final NamedConceptWithOrigin concept = schema.getConcept(childName);
            if (null != concept && concept.getOrigin().isInternal()) {
                concept.accept(this);
            }
        }
    }

    @Override
    public void visit(final Element element) {
        final QName typeName = element.getDataType();
        final NamedConceptWithOrigin type = schema.getConcept(typeName);
        if (null == type || !type.getOrigin().isInternal()) {
            return;
        }
        consume(ParticleType.ELEMENT, typeName);
        type.accept(this);
    }

    @Override
    public void visit(final Type.Group group) {
        consume(ParticleType.GROUP, group.getName());
        group.getParticle().accept(this);
    }

    @Override
    public void visit(final Type.Complex complex) {
        consume(ParticleType.COMPLEX_TYPE, complex.getName());
        complex.getParticle().accept(this);
    }

    @Override
    public void visit(final Type.Simple.Restriction restriction) {
        consume(ParticleType.SIMPLE_TYPE_RESTRICTION, restriction.getName());
    }

    @Override
    public void visit(final Type.Simple.Enumeration enumeration) {
        consume(ParticleType.SIMPLE_TYPE_ENUMERATION, enumeration.getName());
    }

    @Override
    public void visit(final Type.Simple.List list) {
        consume(ParticleType.SIMPLE_TYPE_LIST, list.getName());
    }

    @Override
    public void visit(final Type.Simple.Union union) {
        consume(ParticleType.SIMPLE_TYPE_UNION, union.getName());
    }

    @Override
    public void visit(final Attributes.GlobalAttributeGroupDeclaration globalAttributeGroupDeclaration) {
        consume(ParticleType.GLOBAL_ATTRIBUTE_GROUP, globalAttributeGroupDeclaration.getName());
        for (final Attributes.AttributeOrAttributeGroup attributeOrAttributeGroup : globalAttributeGroupDeclaration.getAttributes().values()) {
            attributeOrAttributeGroup.accept(this);
        }
    }

    @Override
    public void visit(final Attributes.GlobalAttributeDeclaration globalAttributeDeclaration) {
        consume(ParticleType.GLOBAL_ATTRIBUTE, globalAttributeDeclaration.getName());
        final QName typeName = globalAttributeDeclaration.getType();
        final NamedConceptWithOrigin type = schema.getConcept(typeName);
        if (null == type || !type.getOrigin().isInternal()) {
            return;
        }
        type.accept(this);
    }

    @Override
    public void visit(final Attributes.LocalAttribute localAttribute) {
        final QName typeName = localAttribute.getLocalAttributeDeclaration().getType();
        final NamedConceptWithOrigin type = schema.getConcept(typeName);
        if (null == type || !type.getOrigin().isInternal()) {
            return;
        }
        type.accept(this);
    }

    @Override
    public void visit(final Attributes.GlobalAttribute globalAttribute) {
        final QName attributeName = globalAttribute.getGlobalAttributeDeclarationName();
        final NamedConceptWithOrigin attribute = schema.getConcept(attributeName);
        if (null == attribute || !attribute.getOrigin().isInternal()) {
            return;
        }
        consume(ParticleType.GLOBAL_ATTRIBUTE, attributeName);
        attribute.accept(this);
    }

    @Override
    public void visit(final Attributes.AttributeGroup attributeGroup) {
        final QName groupName = attributeGroup.getAttributeGroupDeclarationName();
        final NamedConceptWithOrigin group = schema.getConcept(groupName);
        if (null == group || !group.getOrigin().isInternal()) {
            return;
        }
        consume(ParticleType.GLOBAL_ATTRIBUTE_GROUP, groupName);
        group.accept(this);
    }
}
