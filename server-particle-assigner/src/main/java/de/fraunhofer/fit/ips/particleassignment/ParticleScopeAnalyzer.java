package de.fraunhofer.fit.ips.particleassignment;

import com.google.common.collect.Sets;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class ParticleScopeAnalyzer {
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
}
