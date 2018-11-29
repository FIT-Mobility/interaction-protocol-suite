package de.fraunhofer.fit.ips.testmonitor.exception;

import de.fraunhofer.fit.ips.testmonitor.Visitor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface MonitorExceptionVisitor extends Visitor {
    void visit(IllegalTopicException illegalTopicException);

    void visit(MissingFunctionInfoException missingFunctionInfoException);

    void visit(NoCorrelationIdException noCorrelationIdException);

    void visit(WrappingMonitorException wrappingMonitorException);
}
