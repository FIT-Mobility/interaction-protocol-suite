package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.fraunhofer.fit.omp.reportgenerator.model.template.Function;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.AttributeVisitor;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Attributes;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Choice;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Element;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.ElementList;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.GroupRef;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.NamedConceptWithOriginVisitor;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Sequence;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.SequenceOrChoiceOrGroupRefOrElementList;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.SequenceOrChoiceOrGroupRefOrElementListVisitor;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class DependencyAnalyzer {
    // we use the abbreviation RRT or rrDataType to denote types used as direct parameters of functions
    // RR = request / response, T = type

    @RequiredArgsConstructor
    @Getter
    public static class DependencyHelper {
        final Set<QName> localConceptNames;
        final Set<QName> commonConceptNames;
        final Map<QName, LinkedHashSet<QName>> localRRTypeToDependencies;
    }

    public static DependencyHelper analyze(final Schema schema) {
        final Iterable<Function> operations = schema.getOperations().values();
        final Set<QName> localConceptNames
                = schema.getConcepts().values().stream()
                        .filter(ct -> ct.getOrigin().isInternal())
                        .map(NamedConceptWithOrigin::getName)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        final Map<QName, List<Function>> dataTypeToUsingFunctions = new HashMap<>();
        for (final Function operation : operations) {
            addOperationToNameMapIfNonNull(dataTypeToUsingFunctions, operation, operation.getInputDataType());
            addOperationToNameMapIfNonNull(dataTypeToUsingFunctions, operation, operation.getOutputDataType());
        }

        // analyze all _internal_ types used as request/response type
        final HashMap<QName, LinkedHashSet<QName>> localRRTypeToDependencies = analyze(schema,
                Sets.filter(dataTypeToUsingFunctions.keySet(), localConceptNames::contains)
        );

        final Set<QName> nonCommonTypesNames = new HashSet<>();
        for (final Map.Entry<QName, LinkedHashSet<QName>> entry : localRRTypeToDependencies.entrySet()) {
            final QName localRRTypeName = entry.getKey();
            // if a data type is used as request/response type of a function more than once, it is a common type
            // in this case, also all of its dependencies become common types
            // size can't be 0 since localRRTypeToDependencies and dataTypeToUsingFunctions both only contain types used by functions
            if (dataTypeToUsingFunctions.getOrDefault(localRRTypeName, Collections.emptyList()).size() > 1) {
                continue;
            }
            nonCommonTypesNames.add(localRRTypeName);
            nonCommonTypesNames.addAll(entry.getValue());
        }

        final Set<QName> commonConceptNames = ImmutableSet.copyOf(Sets.difference(localConceptNames, nonCommonTypesNames));
        return new DependencyHelper(localConceptNames, commonConceptNames, localRRTypeToDependencies);
    }

    private static void addOperationToNameMapIfNonNull(Map<QName, List<Function>> dataTypeToUsingFunctions,
                                                       Function operation, Type.Complex request) {
        if (request != null) {
            dataTypeToUsingFunctions.computeIfAbsent(request.getName(), ignored -> new LinkedList<>()).add(operation);
        }
    }

    public static HashMap<QName, LinkedHashSet<QName>> analyze(final Schema schema, final Iterable<QName> rrDataTypes) {
        final HashMap<QName, HashSet<QName>> childToRRT = new HashMap<>();
        final HashMap<QName, LinkedHashSet<QName>> rrtToFlattenedChildren = new HashMap<>();

        for (final QName rrDataType : rrDataTypes) {
            final LinkedHashSet<QName> flattenedChildren = new LinkedHashSet<>();
            // add this here already to make sure that the keySet of rrtToFlattenedChildren contains all rrDataTypes
            rrtToFlattenedChildren.put(rrDataType, flattenedChildren);

            final Type.Complex complexType = (Type.Complex) schema.getConcepts().get(rrDataType);
            final SequenceOrChoiceOrGroupRefOrElementList particle = complexType.getParticle();
            if (particle == null) {
                continue;
            }

            // go through the elements looking for dependent concepts
            final MySequenceOrChoiceOrGroupRefOrElementListVisitor visitor = new MySequenceOrChoiceOrGroupRefOrElementListVisitor(schema, childToRRT, rrDataType, flattenedChildren);
            particle.accept(visitor);
            // go through the attributes looking for dependent concepts
            for (final Attributes.AttributeOrAttributeGroup attributeOrAttributeGroup : complexType.getAttributes().getAttributes().values()) {
                attributeOrAttributeGroup.accept(visitor);
            }
        }

        final List<QName> conceptsWithSingleRRTOcc = childToRRT.entrySet()
                                                               .stream()
                                                               .filter(e -> 1 == e.getValue().size())
                                                               .map(Map.Entry::getKey)
                                                               .collect(Collectors.toList());

        rrtToFlattenedChildren.forEach((key, value) -> value.removeIf(
                ((Predicate<QName>) conceptsWithSingleRRTOcc::contains).negate()
        ));

        return rrtToFlattenedChildren;
    }

    @RequiredArgsConstructor
    private static class MySequenceOrChoiceOrGroupRefOrElementListVisitor implements SequenceOrChoiceOrGroupRefOrElementListVisitor, NamedConceptWithOriginVisitor, AttributeVisitor {
        private final Schema schema;
        private final HashMap<QName, HashSet<QName>> childToRRT;
        private final QName rrDataType;
        private final LinkedHashSet<QName> flattenedChildren;

        private void consume(final QName name) {
            childToRRT.computeIfAbsent(name, ignored -> new HashSet<>()).add(rrDataType);
            flattenedChildren.add(name);
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
            final NamedConceptWithOrigin group = schema.getConcepts().get(refName);
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
                final NamedConceptWithOrigin concept = schema.getConcepts().get(childName);
                if (concept.getOrigin().isInternal()) {
                    concept.accept(this);
                }
            }
        }

        @Override
        public void visit(final Element element) {
            final QName typeName = element.getDataType();
            final NamedConceptWithOrigin type = schema.getConcepts().get(typeName);
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
            final NamedConceptWithOrigin type = schema.getConcepts().get(typeName);
            if (null == type || !type.getOrigin().isInternal()) {
                return;
            }
            consume(typeName);
            type.accept(this);
        }

        @Override
        public void visit(final Attributes.LocalAttribute localAttribute) {
            final QName typeName = localAttribute.getLocalAttributeDeclaration().getType();
            final NamedConceptWithOrigin type = schema.getConcepts().get(typeName);
            if (null == type || !type.getOrigin().isInternal()) {
                return;
            }
            consume(typeName);
            type.accept(this);
        }

        @Override
        public void visit(final Attributes.GlobalAttribute globalAttribute) {
            final QName attributeName = globalAttribute.getGlobalAttributeDeclarationName();
            final NamedConceptWithOrigin attribute = schema.getConcepts().get(attributeName);
            if (null == attribute || !attribute.getOrigin().isInternal()) {
                return;
            }
            consume(attributeName);
            attribute.accept(this);
        }

        @Override
        public void visit(final Attributes.AttributeGroup attributeGroup) {
            final QName groupName = attributeGroup.getAttributeGroupDeclarationName();
            final NamedConceptWithOrigin group = schema.getConcepts().get(groupName);
            if (null == group || !group.getOrigin().isInternal()) {
                return;
            }
            consume(groupName);
            group.accept(this);
        }
    }
}
