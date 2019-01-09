package de.fraunhofer.fit.ips.particleassignment;

import com.google.common.collect.Sets;
import de.fraunhofer.fit.ips.model.template.Assertion;
import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.template.Level;
import de.fraunhofer.fit.ips.model.template.Particle;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.Request;
import de.fraunhofer.fit.ips.model.template.Response;
import de.fraunhofer.fit.ips.model.template.Service;
import de.fraunhofer.fit.ips.model.template.Text;
import de.fraunhofer.fit.ips.model.template.helper.InnerNode;
import de.fraunhofer.fit.ips.model.template.helper.RequestOrResponse;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;
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
import de.fraunhofer.fit.ips.particleassignment.ParticleScopeAnalyzer.ProjectScope.ServiceScope;
import de.fraunhofer.fit.ips.particleassignment.ParticleScopeAnalyzer.ProjectScope.ServiceScope.FunctionScope;
import de.fraunhofer.fit.ips.particleassignment.ParticleScopeAnalyzer.ProjectScope.ServiceScope.FunctionScope.RequestOrResponseScope;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class ParticleScopeAnalyzer {
    public enum ParticleType {
        ELEMENT, COMPLEX_TYPE, GROUP, SIMPLE_TYPE_RESTRICTION, SIMPLE_TYPE_ENUMERATION, SIMPLE_TYPE_UNION,
        SIMPLE_TYPE_LIST, GLOBAL_ATTRIBUTE, GLOBAL_ATTRIBUTE_GROUP
    }

    enum Scope {
        REQUEST_OR_RESPONSE, FUNCTION, SERVICE, PROJECT
    }

    interface Scoped {
        StructureBase getStructuralElement();

        Scope getScope();

        Scoped merge(final Scoped other);
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    static class ProjectScope implements Scoped {
        final Project project;

        @Override
        public Project getStructuralElement() {
            return project;
        }

        @Override
        public Scope getScope() {
            return Scope.PROJECT;
        }

        @Override
        public Scoped merge(final Scoped other) {
            return this;
        }

        @RequiredArgsConstructor
        @EqualsAndHashCode
        class ServiceScope implements Scoped {
            final Service service;

            @Override
            public Service getStructuralElement() {
                return service;
            }

            @Override
            public Scope getScope() {
                return Scope.SERVICE;
            }

            @Override
            public Scoped merge(final Scoped other) {
                switch (other.getScope()) {
                    case PROJECT:
                        return other;
                    case SERVICE:
                        return ((ServiceScope) other).service == service ? this : new ProjectScope(project);
                    case FUNCTION:
                        return ((FunctionScope) other).getService() == service ? this : new ProjectScope(project);
                    case REQUEST_OR_RESPONSE:
                        return ((RequestOrResponseScope) other).getService() == service ? this : new ProjectScope(project);
                }
                throw new IllegalStateException("unknown scope enum value");
            }

            @RequiredArgsConstructor
            @EqualsAndHashCode
            class FunctionScope implements Scoped {
                final Function function;

                @Override
                public Function getStructuralElement() {
                    return function;
                }

                @Override
                public Scope getScope() {
                    return Scope.FUNCTION;
                }

                Service getService() {
                    return service;
                }

                @Override
                public Scoped merge(final Scoped other) {
                    switch (other.getScope()) {
                        case FUNCTION:
                            return ((FunctionScope) other).function == function ? this : new ServiceScope(service).merge(other);
                        case REQUEST_OR_RESPONSE:
                            return ((RequestOrResponseScope) other).getFunction() == function ? this : new ServiceScope(service).merge(other);
                    }
                    return other.merge(this);
                }

                @RequiredArgsConstructor
                @EqualsAndHashCode
                class RequestOrResponseScope implements Scoped {
                    final RequestOrResponse requestOrResponse;

                    @Override
                    public RequestOrResponse getStructuralElement() {
                        return requestOrResponse;
                    }

                    @Override
                    public Scope getScope() {
                        return Scope.REQUEST_OR_RESPONSE;
                    }

                    Service getService() {
                        return service;
                    }

                    Function getFunction() {
                        return function;
                    }

                    @Override
                    public Scoped merge(final Scoped other) {
                        if (Scope.REQUEST_OR_RESPONSE == other.getScope()) {
                            return ((RequestOrResponseScope) other).requestOrResponse == requestOrResponse ? this : new FunctionScope(function).merge(other);
                        }
                        return other.merge(this);
                    }
                }
            }
        }
    }

    public static Map<StructureBase, List<QName>> categorizeDanglingParticles(final Schema schema,
                                                                              final Project project,
                                                                              final EnumSet<ParticleType> consideredParticleTypes) {
        final ProjectScope projectScope = new ProjectScope(project);

        // create a map from all particles within the document structure to their scope
        // since data types are only put to a single location within the document structure, the scope is unique
        final Map<QName, WrappedScope<? extends Scoped>> assignedParticleToScope = new HashMap<>();
        for (final StructureBase child : project.getChildren()) {
            child.accept(new ProjectScopeVisitor(assignedParticleToScope, new WrappedScope<>(projectScope, true)));
        }

        // analyze dependencies of assignedParticles searching for danglingParticles
        final Map<QName, Scoped> danglingParticleToScope = new HashMap<>();
        for (final Map.Entry<QName, WrappedScope<? extends Scoped>> entry : assignedParticleToScope.entrySet()) {
            final QName assignedParticleName = entry.getKey();
            // at this point, it is irrelevant whether the scope was explicit or implicit
            final Scoped scope = entry.getValue().getScope();
            final NamedConceptWithOrigin namedConceptWithOrigin = schema.getConcept(assignedParticleName);
            if (null == namedConceptWithOrigin) {
                // OUCH
                log.error("No concept found for referenced particle {}", assignedParticleName);
                continue;
            }
            namedConceptWithOrigin.accept(new DependenciesVisitor(schema, consideredParticleTypes,
                    danglingParticle -> danglingParticleToScope.merge(danglingParticle, scope, Scoped::merge)));
        }

        // all unassigned particles not referenced within assigned particles belong to the project scope
        final FilteringProjectScopeAssigner filteringProjectScopeAssigner = new FilteringProjectScopeAssigner(consideredParticleTypes, danglingParticleToScope, projectScope);
        for (final QName danglingParticle : Sets.difference(schema.getInternalConceptNames(), assignedParticleToScope.keySet())) {
            if (danglingParticleToScope.containsKey(danglingParticle)) {
                continue;
            }
            final NamedConceptWithOrigin namedConceptWithOrigin = schema.getConcept(danglingParticle);
            assert namedConceptWithOrigin != null : "Schema::internalConceptNames is supposed to be a subset of the keySet of Schema::concepts";
            namedConceptWithOrigin.accept(filteringProjectScopeAssigner);
        }

        return danglingParticleToScope.entrySet().stream().collect(
                Collectors.groupingBy(entry -> entry.getValue().getStructuralElement(),
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    @RequiredArgsConstructor
    static class FilteringProjectScopeAssigner implements NamedConceptWithOriginVisitor {
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

    @RequiredArgsConstructor
    private static class DependenciesVisitor implements SequenceOrChoiceOrGroupRefOrElementListVisitor, NamedConceptWithOriginVisitor, AttributeVisitor {
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


    private static class ProjectScopeVisitor extends ScopedStructureVisitor<ProjectScope> {
        ProjectScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                            final WrappedScope<ProjectScope> projectScope) {
            super(particleToScope, projectScope);
        }

        @Override
        public void visit(final Service service) {
            final ServiceScopeVisitor serviceScopeVisitor = new ServiceScopeVisitor(particleToScope, new WrappedScope<>(wrappedScope.scope.new ServiceScope(service), true));
            for (final StructureBase child : service.getChildren()) {
                child.accept(serviceScopeVisitor);
            }
        }
    }

    private static class ServiceScopeVisitor extends ScopedStructureVisitor<ServiceScope> {
        ServiceScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                            final WrappedScope<ServiceScope> wrappedScope) {
            super(particleToScope, wrappedScope);
        }

        @Override
        public void visit(final Function function) {
            final FunctionScopeVisitor functionScopeVisitor = new FunctionScopeVisitor(particleToScope, new WrappedScope<>(wrappedScope.scope.new FunctionScope(function), true));
            for (final StructureBase child : function.getChildren()) {
                child.accept(functionScopeVisitor);
            }
        }
    }

    private static class FunctionScopeVisitor extends ScopedStructureVisitor<FunctionScope> {
        FunctionScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                             final WrappedScope<FunctionScope> wrappedScope) {
            super(particleToScope, wrappedScope);
        }

        private <RR extends RequestOrResponse & InnerNode> void handle(final RR requestOrResponse) {
            final RequestOrResponseScopeVisitor requestOrResponseScopeVisitor = new RequestOrResponseScopeVisitor(particleToScope, new WrappedScope<>(wrappedScope.scope.new RequestOrResponseScope(requestOrResponse), true));
            for (final StructureBase child : requestOrResponse.getChildren()) {
                child.accept(requestOrResponseScopeVisitor);
            }
        }

        @Override
        public void visit(final Request request) {
            handle(request);
        }

        @Override
        public void visit(final Response response) {
            handle(response);
        }
    }

    private static class RequestOrResponseScopeVisitor extends ScopedStructureVisitor<RequestOrResponseScope> {
        RequestOrResponseScopeVisitor(final Map<QName, WrappedScope<? extends Scoped>> particleToScope,
                                      final WrappedScope<RequestOrResponseScope> wrappedScope) {
            super(particleToScope, wrappedScope);
            registerImplicit(wrappedScope.getScope().getStructuralElement().getParticle());
        }
    }

    @Data
    static class WrappedScope<S extends Scoped> {
        final S scope;
        final boolean explicit;

        static WrappedScope<? extends Scoped> merge(final WrappedScope<? extends Scoped> oldValue,
                                                    final WrappedScope<? extends Scoped> newValue) {
            if (oldValue.explicit) {
                return oldValue;
            }
            assert !newValue.isExplicit();
            return new WrappedScope<>(oldValue.scope.merge(newValue.scope), false);
        }
    }

    @RequiredArgsConstructor
    private static abstract class ScopedStructureVisitor<S extends Scoped> implements StructureVisitor {
        final Map<QName, WrappedScope<? extends Scoped>> particleToScope;
        final WrappedScope<S> wrappedScope;

        protected void registerExplicit(final QName particleName) {
            particleToScope.put(particleName, wrappedScope);
        }

        protected void registerImplicit(final QName particleName) {
            particleToScope.merge(particleName, new WrappedScope<>(wrappedScope.scope, false), WrappedScope::merge);
        }

        @Override
        public void visit(final Level level) {
            for (final StructureBase child : level.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visit(final Particle particle) {
            registerExplicit(particle.getName());
        }

        @Override
        public void visit(final Assertion assertion) {
        }

        @Override
        public void visit(final Function function) {
        }

        @Override
        public void visit(final Project project) {
        }

        @Override
        public void visit(final Request request) {
        }

        @Override
        public void visit(final Response response) {
        }

        @Override
        public void visit(final Service service) {
        }

        @Override
        public void visit(final Text text) {
        }
    }
}
