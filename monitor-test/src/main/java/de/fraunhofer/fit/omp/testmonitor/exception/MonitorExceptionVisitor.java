package de.fraunhofer.fit.omp.testmonitor.exception;

import de.fraunhofer.fit.omp.testmonitor.Visitor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface MonitorExceptionVisitor extends Visitor {
    void visit(IllegalTopicException illegalTopicException);

    void visit(MissingFunctionInfoException missingFunctionInfoException);

    void visit(NoCorrelationIdException noCorrelationIdException);

    void visit(WrappingMonitorException wrappingMonitorException);
}
