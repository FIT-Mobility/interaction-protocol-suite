package de.fraunhofer.fit.ips.reportgenerator.model.web;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2017
 */
public class NormalResponse extends Response {

    public NormalResponse() {
        super();
        addField("success", true);
    }

}
