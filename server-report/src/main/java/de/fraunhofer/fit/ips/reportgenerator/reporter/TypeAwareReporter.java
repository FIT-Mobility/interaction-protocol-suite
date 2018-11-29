package de.fraunhofer.fit.ips.reportgenerator.reporter;

import de.fraunhofer.fit.ips.reportgenerator.ReportType;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.11.2017
 */
public interface TypeAwareReporter {
    ReportType getType();
}
