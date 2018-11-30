package de.fraunhofer.fit.ips.model.xsd;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface SequenceOrChoiceVisitor extends SequenceOrChoiceOrGroupRefVisitor {
    @Override
    default void visit(final GroupRef groupRef) {
        throw new Error();
    }
}
