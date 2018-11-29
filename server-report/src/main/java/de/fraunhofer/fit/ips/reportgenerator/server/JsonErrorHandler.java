package de.fraunhofer.fit.ips.reportgenerator.server;

import com.google.common.net.HttpHeaders;
import de.fraunhofer.fit.ips.reportgenerator.model.web.ErrorResponse;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.11.2017
 */
public class JsonErrorHandler extends ErrorHandler {
    @Override
    protected void generateAcceptableResponse(Request baseRequest, HttpServletRequest request,
                                              HttpServletResponse response, int code, String message,
                                              String mimeType) throws IOException {
        switch (mimeType) {
            case "application/json":
                baseRequest.setHandled(true);
                generateJson(response, message);
                break;
            default: {
                super.generateAcceptableResponse(baseRequest, request, response, code, message, mimeType);
            }
        }
    }

    private void generateJson(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline");
        response.getWriter().write(new ErrorResponse(message).toJsonString());
    }
}
