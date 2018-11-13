package de.fraunhofer.fit.omp.reportgenerator.model.web;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.fit.omp.reportgenerator.converter.JsonObjectMapper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.parser.Validator;
import de.fraunhofer.fit.omp.reportgenerator.server.ErrorMessages;

import java.util.Collection;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 20.03.2018
 */
public class XSDErrorResponse extends ErrorResponse {

    public XSDErrorResponse(Collection<Validator.ValidationError> errors) {
        super(ErrorMessages.INVALID_XSD);
        json.set("errors", convertToJson(errors));
    }

    private static ArrayNode convertToJson(Collection<Validator.ValidationError> errors) {
        ArrayNode arrayNode = JsonObjectMapper.INSTANCE.get().createArrayNode();
        for (Validator.ValidationError error : errors) {
            ObjectNode jsonError = JsonObjectMapper.INSTANCE.get().createObjectNode();
            final Validator.ValidationError.ErrorLocation location = error.getLocation();
            jsonError.put("line", location.getLine());
            jsonError.put("column", location.getColumn());
            jsonError.put("message", error.getMessage());
            arrayNode.add(jsonError);
        }
        return arrayNode;
    }

}
