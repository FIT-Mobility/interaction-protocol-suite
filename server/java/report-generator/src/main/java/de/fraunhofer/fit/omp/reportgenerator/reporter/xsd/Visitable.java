package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface Visitable<T extends Visitor> {
    void accept(final T visitor);
}
