package de.fraunhofer.fit.ips.reportgenerator.model.xsd;

import de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.Visitable;

/**
 * Represents a sequence, choice, group or element
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.01.2018
 */
public interface SequenceOrChoiceOrGroupRefOrElementList extends Visitable<SequenceOrChoiceOrGroupRefOrElementListVisitor> {
}
