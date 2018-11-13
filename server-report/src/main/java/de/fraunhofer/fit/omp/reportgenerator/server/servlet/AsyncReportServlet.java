package de.fraunhofer.fit.omp.reportgenerator.server.servlet;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import de.fraunhofer.fit.omp.reportgenerator.ApplicationConfig;
import de.fraunhofer.fit.omp.reportgenerator.ReportType;
import de.fraunhofer.fit.omp.reportgenerator.Utils;
import de.fraunhofer.fit.omp.reportgenerator.model.ReportWrapper;
import de.fraunhofer.fit.omp.reportgenerator.model.web.NormalResponse;
import de.fraunhofer.fit.omp.reportgenerator.server.ErrorMessages;
import de.fraunhofer.fit.omp.reportgenerator.service.AsyncReportService;
import de.fraunhofer.fit.omp.reportgenerator.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.fraunhofer.fit.omp.reportgenerator.server.ErrorMessages.REPORT_ID_NOT_SET;
import static de.fraunhofer.fit.omp.reportgenerator.server.ErrorMessages.REPORT_NOT_FOUND;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2017
 */
@Slf4j
public class AsyncReportServlet extends AbstractBaseServlet {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();

    private final ReportService reportService;
    private final AsyncReportService asyncReportService;
    private final ApplicationConfig config;

    public AsyncReportServlet(ReportService reportService, ApplicationConfig config) {
        this.reportService = reportService;
        this.asyncReportService = new AsyncReportService(reportService);
        this.config = config;
    }

    @Override
    public void destroy() {
        reportService.destroy();
    }

    /**
     * The expected path is .../report-async/{report-id}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (Strings.isNullOrEmpty(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, REPORT_ID_NOT_SET);
            return;
        }

        String reportId = pathInfo.replaceFirst("/", "");
        if (Strings.isNullOrEmpty(reportId)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, REPORT_ID_NOT_SET);
            return;
        }

        ReportWrapper reportWrapper = asyncReportService.getReport(reportId);
        if (reportWrapper == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, REPORT_NOT_FOUND);
            return;
        }

        if (!reportWrapper.errors.isEmpty()) {
            reportWrapper.errors.forEach(i -> resp.setHeader(HttpHeaders.WARNING, i));
        }
        resp.setContentType(reportWrapper.type.contentType);
        resp.getOutputStream().write(reportWrapper.report);
    }

    @Override
    protected void doPostHandle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        processReportPost(req, resp);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    private void processReportPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Set<String> values = getHeaderValues(req, HttpHeaders.ACCEPT);
        List<ReportType> reportTypes = extractReportTypes(values);

        if (reportTypes.size() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessages.CONTAINS_NO_REPORT_TYPE);
            return;
        }

        String jsonAsString = Utils.readToString(req.getInputStream());
        NormalResponse nr = new NormalResponse();

        String templateId;
        Set<String> templateIds = getHeaderValues(req, config.getReportingApiTemplateIdHeader());
        if (templateIds.size() == 0) {
            templateId = config.getReportingDocxTemplate();
        } else if (templateIds.size() == 1) {
            templateId = Iterables.getOnlyElement(templateIds);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorMessages.MULTIPLE_TEMPLATES);
            return;
        }

        reportTypes.parallelStream()
                   .forEach(type -> {
                       String reportId = process(type, templateId, jsonAsString);
                       nr.addField(type.contentType, reportId);
                   });

        writeResponse(nr, resp);
    }

    private Set<ReportType> getSupportedTypes() {
        return asyncReportService.getSupportedTypes();
    }

    private String process(ReportType reportType, String templateId, String jsonAsString) {
        try {
            return asyncReportService.generate(reportType, templateId, jsonAsString);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> getHeaderValues(HttpServletRequest req, String name) {
        Enumeration<String> valueEnums = req.getHeaders(name);
        Set<String> valueList = new HashSet<>();
        while (valueEnums.hasMoreElements()) {
            COMMA_SPLITTER.split(valueEnums.nextElement())
                          .forEach(valueList::add);
        }
        return valueList;
    }

    private List<ReportType> extractReportTypes(Set<String> mimeHeaders) {
        Set<ReportType> supportedTypes = getSupportedTypes();
        List<ReportType> reportTypes = new ArrayList<>(supportedTypes.size());
        for (String mimeHeader : mimeHeaders) {
            for (ReportType reportType : supportedTypes) {
                if (reportType.contentType.equals(mimeHeader)) {
                    reportTypes.add(reportType);
                }
            }
        }
        return reportTypes;
    }
}
