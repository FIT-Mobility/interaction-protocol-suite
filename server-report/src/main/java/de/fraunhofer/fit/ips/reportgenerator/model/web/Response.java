package de.fraunhofer.fit.ips.reportgenerator.model.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.fit.ips.reportgenerator.converter.JsonObjectMapper;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 20.03.2018
 */
public class Response {

    protected final ObjectNode json;

    public Response() {
        json = JsonObjectMapper.INSTANCE.get().createObjectNode();
    }

    public void addField(String key, boolean val) {
        json.put(key, val);
    }

    public void addField(String key, String val) {
        json.put(key, val);
    }

    public String toJsonString() {
        return json.toString();
    }
}
