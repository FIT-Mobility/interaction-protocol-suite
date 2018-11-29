package de.fraunhofer.fit.ips.reportgenerator.model;

import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Schema;
import fr.opensagres.xdocreport.template.IContext;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.01.2018
 */
public interface ReportContext extends IContext {
    Schema getSchema();
}
