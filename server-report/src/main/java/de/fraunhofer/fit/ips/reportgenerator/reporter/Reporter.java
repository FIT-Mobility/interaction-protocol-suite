package de.fraunhofer.fit.ips.reportgenerator.reporter;

import de.fraunhofer.fit.ips.reportgenerator.Utils;
import de.fraunhofer.fit.ips.reportgenerator.model.ReportWrapper;

import java.io.InputStream;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.11.2017
 */
public interface Reporter extends TypeAwareReporter {

    ReportWrapper report(String templateId, String jsonAsString) throws Exception;

    default ReportWrapper report(String templateId, InputStream jsonAsStream) throws Exception {
        return report(templateId, Utils.readToString(jsonAsStream));
    }

    default void shutDown() {
        // no-op
    }
}
