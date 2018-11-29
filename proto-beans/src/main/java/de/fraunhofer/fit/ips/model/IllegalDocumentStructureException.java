package de.fraunhofer.fit.ips.model;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class IllegalDocumentStructureException extends Exception {
    public IllegalDocumentStructureException() {
        super();
    }

    public IllegalDocumentStructureException(final String message) {
        super(message);
    }

    public IllegalDocumentStructureException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IllegalDocumentStructureException(final Throwable cause) {
        super(cause);
    }

    protected IllegalDocumentStructureException(final String message, final Throwable cause,
                                                final boolean enableSuppression,
                                                final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
