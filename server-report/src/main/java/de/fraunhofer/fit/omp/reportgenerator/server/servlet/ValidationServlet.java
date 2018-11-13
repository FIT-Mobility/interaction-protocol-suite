package de.fraunhofer.fit.omp.reportgenerator.server.servlet;

import de.fraunhofer.fit.omp.reportgenerator.Utils;
import de.fraunhofer.fit.omp.reportgenerator.model.web.NormalResponse;
import de.fraunhofer.fit.omp.reportgenerator.model.web.Response;
import de.fraunhofer.fit.omp.reportgenerator.model.web.XSDErrorResponse;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.parser.Validator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 02.10.2018
 */
@Slf4j
public class ValidationServlet extends AbstractBaseServlet {

    @Override
    protected void doPostHandle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        processValidationPost(req, resp);
    }

    private void processValidationPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String jsonAsString = Utils.readToString(req.getInputStream());
        Collection<Validator.ValidationError> errors = Validator.validate(jsonAsString);

        Response jsonResponse;
        int statusCode;
        if (errors.isEmpty()) {
            jsonResponse = new NormalResponse();
            statusCode = HttpServletResponse.SC_OK;
        } else {
            jsonResponse = new XSDErrorResponse(errors);
            statusCode = HttpServletResponse.SC_BAD_REQUEST;
        }

        writeResponse(jsonResponse, resp, statusCode);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
