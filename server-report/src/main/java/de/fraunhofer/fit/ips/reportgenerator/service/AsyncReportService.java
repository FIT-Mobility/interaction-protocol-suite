package de.fraunhofer.fit.ips.reportgenerator.service;

import de.fraunhofer.fit.ips.reportgenerator.reporter.AsyncReporter;
import de.fraunhofer.fit.ips.reportgenerator.reporter.AsyncReporterFromMemory;
import de.fraunhofer.fit.ips.reportgenerator.ReportType;
import de.fraunhofer.fit.ips.reportgenerator.model.ReportWrapper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.11.2017
 */
public class AsyncReportService {

    private final HashMap<ReportType, AsyncReporter> asyncReporterMap = new HashMap<>();

    public AsyncReportService(ReportService reportService) {
        reportService.getReporterMap()
                     .forEach((type, reporter) -> asyncReporterMap.put(type, new AsyncReporterFromMemory(reporter)));
    }

    @Nullable
    public ReportWrapper getReport(String key) {
        for (Map.Entry<ReportType, AsyncReporter> entry : asyncReporterMap.entrySet()) {
            ReportWrapper data = entry.getValue().get(key);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    public String generate(ReportType type, String templateId, String model) throws Exception {
        return findReporter(type).generate(templateId, model);
    }

    public Set<ReportType> getSupportedTypes() {
        return asyncReporterMap.keySet();
    }

    private AsyncReporter findReporter(ReportType type) {
        AsyncReporter r = asyncReporterMap.get(type);
        if (r == null) {
            throw new RuntimeException("Reporter for '" + type.contentType + "' not found");
        }
        return r;
    }
}
