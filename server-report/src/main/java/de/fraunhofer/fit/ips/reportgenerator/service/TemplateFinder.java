package de.fraunhofer.fit.ips.reportgenerator.service;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.02.2018
 */
public interface TemplateFinder {
    IXDocReport getTemplate(String templateId);
    void loadTemplate(InputStream in, String templateId) throws IOException, XDocReportException;
}
