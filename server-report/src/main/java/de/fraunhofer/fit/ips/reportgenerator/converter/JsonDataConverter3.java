package de.fraunhofer.fit.ips.reportgenerator.converter;

import de.fraunhofer.fit.ips.reportgenerator.model.ReportContext;
import fr.opensagres.xdocreport.document.IXDocReport;

/**
 * @author Fabian Ohler <ohler@dbis.rwth-aachen.de>
 * @since 01.12.2017
 */
public class JsonDataConverter3 implements ModelConverter {

    private static final String PROJECT = "project";

    @Override
    public ReportContext getContext(IXDocReport report, String jsonAsString) throws Exception {
        return null;
    }
}
