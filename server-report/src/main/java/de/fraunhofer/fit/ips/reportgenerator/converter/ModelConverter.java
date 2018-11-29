package de.fraunhofer.fit.ips.reportgenerator.converter;

import de.fraunhofer.fit.ips.reportgenerator.model.ReportContext;
import fr.opensagres.xdocreport.document.IXDocReport;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.11.2017
 */
public interface ModelConverter {
    ReportContext getContext(final IXDocReport report, final String json) throws Exception;
}
