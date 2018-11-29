package de.fraunhofer.fit.ips.reportgenerator.reporter;

import de.fraunhofer.fit.ips.reportgenerator.Utils;
import de.fraunhofer.fit.ips.reportgenerator.model.ReportWrapper;

import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.11.2017
 */
public interface AsyncReporter extends TypeAwareReporter {

    /**
     * @return the generated report for this report id, or null if it is not found.
     */
    @Nullable
    ReportWrapper get(String id);

    /**
     * @return id of the generated report, which can be used later for retrieval.
     */
    String generate(String templateId, String jsonAsString) throws Exception;

    /**
     * @return id of the generated report, which can be used later for retrieval.
     */
    default String generate(String templateId, InputStream jsonAsStream) throws Exception {
        return generate(templateId, Utils.readToString(jsonAsStream));
    }
}
