package de.fraunhofer.fit.ips.model.xsd;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface SequenceOrChoiceOrGroupRefVisitor extends SequenceOrChoiceOrGroupRefOrElementListVisitor {
    @Override
    default void visit(final ElementList elementList) {
        throw new Error();
    }
}
