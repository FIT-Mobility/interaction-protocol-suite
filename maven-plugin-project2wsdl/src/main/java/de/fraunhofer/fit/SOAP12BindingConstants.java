package de.fraunhofer.fit;


import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class SOAP12BindingConstants {
    public static final String NS = "http://schemas.xmlsoap.org/wsdl/soap12/";

    public static final QName EL_BINDING = new QName(NS, "binding");
    public static final QName EL_OPERATION = new QName(NS, "operation");
    public static final QName EL_BODY = new QName(NS, "body");
    public static final QName EL_FAULT = new QName(NS, "fault");
    public static final QName EL_HEADER = new QName(NS, "header");
    public static final QName EL_HEADER_FAULT = new QName(NS, "headerfault");
    public static final QName EL_ADDRESS = new QName(NS, "address");

    public static final QName ATT_TRANSPORT = new QName("transport");
    public static final QName ATT_STYLE = new QName("style");
    public static final QName ATT_SOAP_ACTION = new QName("soapAction");
    public static final QName ATT_SOAP_ACTION_REQUIRED = new QName("soapActionRequired");
    public static final QName ATT_ENCODING_STYLE = new QName("encodingStyle");
    public static final QName ATT_USE = new QName("use");
    public static final QName ATT_NAMESPACE = new QName("namespace");
    public static final QName ATT_PARTS = new QName("parts");
    public static final QName ATT_NAME = new QName("name");
    public static final QName ATT_MESSAGE = new QName("message");
    public static final QName ATT_PART = new QName("part");
    public static final QName ATT_LOCATION = new QName("location");

    public static final String STYLE_RPC = "rpc";
    public static final String STYLE_DOCUMENT = "document";

    public static final String USE_LITERAL = "literal";
    public static final String USE_ENCODED = "encoded";
}
