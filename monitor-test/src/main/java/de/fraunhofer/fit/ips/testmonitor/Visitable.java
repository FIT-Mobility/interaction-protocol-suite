package de.fraunhofer.fit.ips.testmonitor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface Visitable<V extends Visitor> {
    void accept(final V visitor);
}
