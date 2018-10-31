package de.fraunhofer.fit;

import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class WSDL11Constants {
    public static final String NS = "http://schemas.xmlsoap.org/wsdl/";

    // root element
    public static final QName EL_DEFINITIONS = new QName(NS, "definitions");

    public static final QName EL_IMPORT = new QName(NS, "import");
    public static final QName EL_TYPES = new QName(NS, "types");
    public static final QName EL_MESSAGE = new QName(NS, "message");
    public static final QName EL_PORT_TYPE = new QName(NS, "portType");
    public static final QName EL_BINDING = new QName(NS, "binding");
    public static final QName EL_SERVICE = new QName(NS, "service");

    public static final QName EL_DOCUMENTATION = new QName(NS, "documentation");

    public static final QName EL_PART = new QName(NS, "part");

    public static final QName EL_OPERATION = new QName(NS, "operation");
    public static final QName EL_INPUT = new QName(NS, "input");
    public static final QName EL_OUTPUT = new QName(NS, "output");
    public static final QName EL_FAULT = new QName(NS, "fault");

    public static final QName EL_PORT = new QName(NS, "port");

    public static final QName ATT_TARGET_NAMESPACE = new QName("targetNamespace");
    public static final QName ATT_NAME = new QName("name");
    public static final QName ATT_ELEMENT = new QName("element");
    public static final QName ATT_TYPE = new QName("type");
    public static final QName ATT_LOCATION = new QName("location");
    public static final QName ATT_MESSAGE = new QName("message");
    public static final QName ATT_BINDING = new QName("binding");

    public static final QName GLOB_ATT_ARRAY_TYPE = new QName(NS, "arrayType");
    public static final QName GLOB_ATT_REQUIRED = new QName(NS, "required");

}
