package de.fraunhofer.fit.ips.reportgenerator.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 04.04.2018
 */
public enum JsonObjectMapper {
    INSTANCE;

    private final ObjectMapper mapper = new ObjectMapper();

    public ObjectMapper get() {
        return mapper;
    }
}
