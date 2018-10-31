package de.fraunhofer.fit.omp.reportgenerator.reporter;

import de.fraunhofer.fit.omp.reportgenerator.ReportType;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.11.2017
 */
public interface TypeAwareReporter {
    ReportType getType();
}
