package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.parser;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.01.2018
 */
public class Utils {

    private static final XSImplementation XS_IMPLEMENTATION;

    static {
        System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            XS_IMPLEMENTATION = (XSImplementation) registry.getDOMImplementation("XS-Loader");
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Apparently, XSLoader instances are not thread-safe. Therefore, we need to create a new one every time.
     */
    static XSLoader createSchemaLoader() {
        XSLoader xsLoader = XS_IMPLEMENTATION.createXSLoader(null);
        xsLoader.getConfig().setParameter("error-handler", LoggingErrorHandler.SINGLETON);
        xsLoader.getConfig().setParameter(
                Constants.XERCES_PROPERTY_PREFIX + Constants.XML_SCHEMA_VERSION_PROPERTY,
                Constants.W3C_XML_SCHEMA11_NS_URI);
        return xsLoader;
    }

}
