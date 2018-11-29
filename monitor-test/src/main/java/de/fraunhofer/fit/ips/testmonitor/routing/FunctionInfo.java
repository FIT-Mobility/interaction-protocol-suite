package de.fraunhofer.fit.ips.testmonitor.routing;

import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface FunctionInfo {
    MEP getMep();

    QName getFunctionElementName();

    String getFunctionName();

    String getServiceName();
}
