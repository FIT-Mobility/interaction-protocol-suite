package de.fraunhofer.fit.ips.vaas;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class VaasConstants {
    private VaasConstants() {
    }

    public static final String HTTP_HEADER_EXCHANGE_ID = "vaas-exchange-id";
    public static final String HTTP_HEADER_MESSAGE_TYPE = "vaas-message-type";

    public static final String HTTP_HEADER_MESSAGE_TYPE_REQUEST = "request";
    public static final String HTTP_HEADER_MESSAGE_TYPE_RESPONSE = "response";
}
