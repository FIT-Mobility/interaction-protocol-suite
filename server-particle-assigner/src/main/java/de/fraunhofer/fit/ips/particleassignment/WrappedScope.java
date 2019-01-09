package de.fraunhofer.fit.ips.particleassignment;

import lombok.Data;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Data
class WrappedScope<S extends Scoped> {
    final S scope;
    final boolean explicit;

    static WrappedScope<? extends Scoped> merge(final WrappedScope<? extends Scoped> oldValue,
                                                final WrappedScope<? extends Scoped> newValue) {
        if (oldValue.explicit) {
            return oldValue;
        }
        assert !newValue.isExplicit();
        return new WrappedScope<>(oldValue.scope.merge(newValue.scope), false);
    }
}
