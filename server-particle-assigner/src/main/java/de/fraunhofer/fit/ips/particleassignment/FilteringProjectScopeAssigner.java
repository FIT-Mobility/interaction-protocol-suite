package de.fraunhofer.fit.ips.particleassignment;

import de.fraunhofer.fit.ips.model.xsd.Attributes;
import de.fraunhofer.fit.ips.model.xsd.Element;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOriginVisitor;
import de.fraunhofer.fit.ips.model.xsd.Type;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.xml.namespace.QName;
import java.util.EnumSet;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class FilteringProjectScopeAssigner implements NamedConceptWithOriginVisitor {
    final EnumSet<ParticleType> consideredParticleTypes;
    final Map<QName, Scoped> danglingParticleToScope;
    final ProjectScope projectScope;

    private void consume(final ParticleType particleType, final QName danglingParticle) {
        if (consideredParticleTypes.contains(particleType)) {
            danglingParticleToScope.put(danglingParticle, projectScope);
        }
    }

    @Override
    public void visit(final Element element) {
        consume(ParticleType.ELEMENT, element.getName());
    }

    @Override
    public void visit(final Type.Group group) {
        consume(ParticleType.GROUP, group.getName());
    }

    @Override
    public void visit(final Type.Complex complex) {
        consume(ParticleType.COMPLEX_TYPE, complex.getName());
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
    }

    @Override
    public void visit(final Attributes.GlobalAttributeDeclaration globalAttributeDeclaration) {
        consume(ParticleType.GLOBAL_ATTRIBUTE, globalAttributeDeclaration.getName());
    }
}
