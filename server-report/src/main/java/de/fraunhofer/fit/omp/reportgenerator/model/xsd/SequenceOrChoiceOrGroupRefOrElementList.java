package de.fraunhofer.fit.omp.reportgenerator.model.xsd;

import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.Visitable;

/**
 * Represents a sequence, choice, group or element
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.01.2018
 */
public interface SequenceOrChoiceOrGroupRefOrElementList extends Visitable<SequenceOrChoiceOrGroupRefOrElementListVisitor> {
}
