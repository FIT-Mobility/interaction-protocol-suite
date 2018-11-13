package de.fraunhofer.fit.omp.reportgenerator.server.servlet;

import de.fraunhofer.fit.omp.reportgenerator.service.TemplateFinder;
import fr.opensagres.xdocreport.core.XDocReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 02.10.2018
 */
@RequiredArgsConstructor
@Slf4j
public class TemplateServlet extends AbstractBaseServlet {

    private final TemplateFinder templateFinder;

    @Override
    protected void doPostHandle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        processTemplatePost(req);
    }

    private void processTemplatePost(HttpServletRequest req) throws IOException, ServletException {
        String templateId = req.getParameter("templateId");
        Part filePart = req.getPart("templateFile");

        try (InputStream content = filePart.getInputStream()) {
            templateFinder.loadTemplate(content, templateId);
        } catch (XDocReportException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected Logger getLogger() {
        return log;
    }
}
