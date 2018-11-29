package de.fraunhofer.fit.ips.reportgenerator.model.web;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.11.2017
 */
public class ErrorResponse extends Response {

    public ErrorResponse(String errorMessage) {
        super();
        addField("success", false);
        addField("msg", errorMessage);
    }
}
