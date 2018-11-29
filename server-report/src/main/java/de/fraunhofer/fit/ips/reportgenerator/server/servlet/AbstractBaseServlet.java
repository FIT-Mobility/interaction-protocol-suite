package de.fraunhofer.fit.ips.reportgenerator.server.servlet;

import com.google.common.util.concurrent.UncheckedExecutionException;
import de.fraunhofer.fit.ips.reportgenerator.exception.RuntimeJsonException;
import de.fraunhofer.fit.ips.reportgenerator.exception.RuntimeXsdException;
import de.fraunhofer.fit.ips.reportgenerator.model.web.Response;
import de.fraunhofer.fit.ips.reportgenerator.server.ErrorMessages;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 02.10.2018
 */
public abstract class AbstractBaseServlet extends HttpServlet {

    protected abstract Logger getLogger();

    /**
     * Actual logic to handle POST requests without exception handling, which is done in {@link
     * #doPost(HttpServletRequest, HttpServletResponse)}.
     */
    protected abstract void doPostHandle(HttpServletRequest req, HttpServletResponse resp) throws Exception;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doPostHandle(req, resp);

            // FIXME: this is dirty, improve later.
            // CachingReporter impl wraps actual exception in an UncheckedExecutionException
        } catch (UncheckedExecutionException e) {
            handleException(e.getCause(), resp);
        } catch (Exception e) {
            handleException(e, resp);
        }
    }

    protected void writeResponse(Response jsonResponse, HttpServletResponse httpResponse) throws IOException {
        writeResponse(jsonResponse, httpResponse, HttpServletResponse.SC_OK);
    }

    protected void writeResponse(Response jsonResponse, HttpServletResponse httpResponse, int statusCode)
            throws IOException {

        httpResponse.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpResponse.setStatus(statusCode);
        httpResponse.getWriter().write(jsonResponse.toJsonString());
    }

    protected void handleException(Throwable t, HttpServletResponse resp) throws IOException {
        getLogger().error("Error occurred", t);

        try {
            throw t;
        } catch (RuntimeJsonException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessages.INVALID_JSON);
        } catch (RuntimeXsdException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessages.INVALID_XSD);
        } catch (Throwable e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorMessages.EXCEPTION_GENERAL);
        }
    }
}
