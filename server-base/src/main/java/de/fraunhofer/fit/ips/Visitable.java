package de.fraunhofer.fit.ips;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface Visitable<T extends Visitor> {
    void accept(final T visitor);
}
