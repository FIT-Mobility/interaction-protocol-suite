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
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class ParticleScopeAnalyzer {

    enum Scope {
        REQUEST_OR_RESPONSE, SERVICE, FUNCTION, PROJECT
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
        public Scoped merge(Scoped other) {
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
            public Scoped merge(Scoped other) {
                switch (other.getScope()) {
                    case PROJECT:
                        return other;
                    case SERVICE:
                    case FUNCTION:
                    case REQUEST_OR_RESPONSE:
                        return ((ServiceScope) other).service == service ? this : new ProjectScope(project);
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

                @Override
                public Scoped merge(Scoped other) {
                    switch (other.getScope()) {
                        case FUNCTION:
                        case REQUEST_OR_RESPONSE:
                            return ((FunctionScope) other).function == function ? this : new ServiceScope(service).merge(other);
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

                    @Override
                    public Scoped merge(Scoped other) {
                        switch (other.getScope()) {
                            case REQUEST_OR_RESPONSE:
                                return ((RequestOrResponseScope) other).requestOrResponse == requestOrResponse ? this : new FunctionScope(function).merge(other);
                        }
                        return other.merge(this);
                    }
                }
            }
        }
    }

    public static Map<StructureBase, List<QName>> categorizeDanglingTypes(final Schema schema, final Project project) {
        final ProjectScope projectScope = new ProjectScope(project);

        // create a map from all data types within the document structure to their scope
        // since data types are only put to a single location within the document structure, the scope is unique
        final Map<QName, Scoped> assignedTypeToScope = new HashMap<>();
        for (final StructureBase child : project.getChildren()) {
            child.accept(new ProjectScopeVisitor(assignedTypeToScope, projectScope));
        }

        final Sets.SetView<QName> danglingTypes = Sets.difference(schema.getInternalConceptNames(), assignedTypeToScope.keySet());
        final Set<QName> assignedTypes = assignedTypeToScope.keySet();

        // analyze dependencies of assignedTypes searching for danglingTypes
        final Map<QName, Scoped> danglingTypeToScope = new HashMap<>();
        for (final Map.Entry<QName, Scoped> entry : assignedTypeToScope.entrySet()) {
            final QName assignedTypeName = entry.getKey();
            final Scoped scope = entry.getValue();
            final NamedConceptWithOrigin namedConceptWithOrigin = schema.getConcept(assignedTypeName);
            if (null == namedConceptWithOrigin) {
                // OUCH
                continue;
            }
            namedConceptWithOrigin.accept(new DependenciesVisitor(schema, assignedTypes, danglingType -> danglingTypeToScope.merge(danglingType, scope, Scoped::merge)));
        }

        // all unassigned types not referenced within assigned types belong to the project scope
        for (final QName danglingType : danglingTypes) {
            danglingTypeToScope.putIfAbsent(danglingType, projectScope);
        }

        return danglingTypeToScope.entrySet().stream().collect(
                Collectors.groupingBy(entry -> entry.getValue().getStructuralElement(),
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
    }

    @RequiredArgsConstructor
    private static class DependenciesVisitor implements SequenceOrChoiceOrGroupRefOrElementListVisitor, NamedConceptWithOriginVisitor, AttributeVisitor {
        private final Schema schema;
        final Set<QName> assignedTypes;
        final Consumer<QName> danglingTypeConsumer;

        private void consume(final QName name) {
            if (assignedTypes.contains(name)) {
                return;
            }
            danglingTypeConsumer.accept(name);
        }

        @Override
        public void visit(final Sequence sequence) {
            final List<SequenceOrChoiceOrGroupRefOrElementList> children = sequence.getParticleList();
            for (final SequenceOrChoiceOrGroupRefOrElementList child : children) {
                child.accept(this);
            }
        }

        @Override
        public void visit(final Choice choice) {
            final List<SequenceOrChoiceOrGroupRefOrElementList> children = choice.getParticleList();
            for (final SequenceOrChoiceOrGroupRefOrElementList child : children) {
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
            consume(refName);
            group.accept(this);
        }

        @Override
        public void visit(final ElementList elementList) {
            final List<Element> elements = elementList.getElements();
            for (final Element element : elements) {
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
            consume(typeName);
            type.accept(this);
        }

        @Override
        public void visit(final Type.Group group) {
            consume(group.getName());
            group.getParticle().accept(this);
        }

        @Override
        public void visit(final Type.Complex complex) {
            consume(complex.getName());
            complex.getParticle().accept(this);
        }

        @Override
        public void visit(final Type.Simple.Restriction restriction) {
            consume(restriction.getName());
        }

        @Override
        public void visit(final Type.Simple.Enumeration enumeration) {
            consume(enumeration.getName());
        }

        @Override
        public void visit(final Type.Simple.List list) {
            consume(list.getName());
        }

        @Override
        public void visit(final Type.Simple.Union union) {
            consume(union.getName());
        }

        @Override
        public void visit(final Attributes.GlobalAttributeGroupDeclaration globalAttributeGroupDeclaration) {
            for (final Attributes.AttributeOrAttributeGroup attributeOrAttributeGroup : globalAttributeGroupDeclaration.getAttributes().values()) {
                attributeOrAttributeGroup.accept(this);
            }
        }

        @Override
        public void visit(final Attributes.GlobalAttributeDeclaration globalAttributeDeclaration) {
            final QName typeName = globalAttributeDeclaration.getType();
            final NamedConceptWithOrigin type = schema.getConcept(typeName);
            if (null == type || !type.getOrigin().isInternal()) {
                return;
            }
            consume(typeName);
            type.accept(this);
        }

        @Override
        public void visit(final Attributes.LocalAttribute localAttribute) {
            final QName typeName = localAttribute.getLocalAttributeDeclaration().getType();
            final NamedConceptWithOrigin type = schema.getConcept(typeName);
            if (null == type || !type.getOrigin().isInternal()) {
                return;
            }
            consume(typeName);
            type.accept(this);
        }

        @Override
        public void visit(final Attributes.GlobalAttribute globalAttribute) {
            final QName attributeName = globalAttribute.getGlobalAttributeDeclarationName();
            final NamedConceptWithOrigin attribute = schema.getConcept(attributeName);
            if (null == attribute || !attribute.getOrigin().isInternal()) {
                return;
            }
            consume(attributeName);
            attribute.accept(this);
        }

        @Override
        public void visit(final Attributes.AttributeGroup attributeGroup) {
            final QName groupName = attributeGroup.getAttributeGroupDeclarationName();
            final NamedConceptWithOrigin group = schema.getConcept(groupName);
            if (null == group || !group.getOrigin().isInternal()) {
                return;
            }
            consume(groupName);
            group.accept(this);
        }
    }


    private static class ProjectScopeVisitor extends ScopedStructureVisitor<ProjectScope> {
        ProjectScopeVisitor(final Map<QName, Scoped> typeToScope, final ProjectScope projectScope) {
            super(typeToScope, projectScope);
        }

        @Override
        public void visit(final Service service) {
            final ServiceScopeVisitor serviceScopeVisitor = new ServiceScopeVisitor(typeToScope, scope.new ServiceScope(service));
            for (final StructureBase child : service.getChildren()) {
                child.accept(serviceScopeVisitor);
            }
        }
    }

    private static class ServiceScopeVisitor extends ScopedStructureVisitor<ServiceScope> {
        ServiceScopeVisitor(final Map<QName, Scoped> typeToScope, final ServiceScope scope) {
            super(typeToScope, scope);
        }

        @Override
        public void visit(final Function function) {
            final FunctionScopeVisitor functionScopeVisitor = new FunctionScopeVisitor(typeToScope, scope.new FunctionScope(function));
            for (final StructureBase child : function.getChildren()) {
                child.accept(functionScopeVisitor);
            }
        }
    }

    private static class FunctionScopeVisitor extends ScopedStructureVisitor<FunctionScope> {
        FunctionScopeVisitor(final Map<QName, Scoped> typeToScope,
                             final FunctionScope scope) {
            super(typeToScope, scope);
        }

        @Override
        public void visit(final Request request) {
            final RequestOrResponseScopeVisitor requestOrResponseScopeVisitor = new RequestOrResponseScopeVisitor(typeToScope, scope.new RequestOrResponseScope(request));
            for (final StructureBase child : request.getChildren()) {
                child.accept(requestOrResponseScopeVisitor);
            }
        }

        @Override
        public void visit(final Response response) {
            final RequestOrResponseScopeVisitor requestOrResponseScopeVisitor = new RequestOrResponseScopeVisitor(typeToScope, scope.new RequestOrResponseScope(response));
            for (final StructureBase child : response.getChildren()) {
                child.accept(requestOrResponseScopeVisitor);
            }
        }
    }

    private static class RequestOrResponseScopeVisitor extends ScopedStructureVisitor<RequestOrResponseScope> {
        RequestOrResponseScopeVisitor(final Map<QName, Scoped> typeToScope,
                                      final RequestOrResponseScope scope) {
            super(typeToScope, scope);
            register(scope.getStructuralElement().getDatatype());
        }
    }

    @RequiredArgsConstructor
    private static abstract class ScopedStructureVisitor<S extends Scoped> implements StructureVisitor {
        final Map<QName, Scoped> typeToScope;
        final S scope;

        protected void register(final QName dataTypeName) {
            typeToScope.put(dataTypeName, scope);
        }

        @Override
        public void visit(final Level level) {
            for (final StructureBase child : level.getChildren()) {
                child.accept(this);
            }
        }

        @Override
        public void visit(final Particle datatype) {
            register(datatype.getName());
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
