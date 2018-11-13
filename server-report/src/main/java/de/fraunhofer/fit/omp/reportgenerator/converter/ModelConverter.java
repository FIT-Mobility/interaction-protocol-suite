package de.fraunhofer.fit.omp.reportgenerator.converter;

import de.fraunhofer.fit.omp.reportgenerator.model.ReportContext;
import fr.opensagres.xdocreport.document.IXDocReport;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.11.2017
 */
public interface ModelConverter {
    ReportContext getContext(IXDocReport report, String jsonAsString) throws Exception;
}
