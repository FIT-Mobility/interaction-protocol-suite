package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.util.DOMErrorHandlerWrapper;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 08.02.2018
 */
@Slf4j
public enum LoggingErrorHandler implements DOMErrorHandler {
    SINGLETON;

    /**
     * Used {@link DOMErrorHandlerWrapper#printError(org.w3c.dom.DOMError)} as inspiration.
     */
    @Override
    public boolean handleError(DOMError error) {
        int severity = error.getSeverity();
        switch (severity) {
            case DOMError.SEVERITY_WARNING:
                log.warn("{}", error.getMessage());
                break;
            case DOMError.SEVERITY_ERROR:
            case DOMError.SEVERITY_FATAL_ERROR:
                log.error("{}", error.getMessage());
                break;
            default:
                throw new RuntimeException("Unexpected DOMError.severity");
        }
        // true => No matter what, try to continue processing
        return true;
    }
}
