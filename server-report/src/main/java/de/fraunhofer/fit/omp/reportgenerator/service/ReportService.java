package de.fraunhofer.fit.omp.reportgenerator.service;

import de.fraunhofer.fit.omp.reportgenerator.ApplicationConfig;
import de.fraunhofer.fit.omp.reportgenerator.ReportType;
import de.fraunhofer.fit.omp.reportgenerator.converter.JsonDataConverter3;
import de.fraunhofer.fit.omp.reportgenerator.converter.ModelConverter;
import de.fraunhofer.fit.omp.reportgenerator.model.ReportWrapper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.CachingReporter;
import de.fraunhofer.fit.omp.reportgenerator.reporter.DocxReporter;
import de.fraunhofer.fit.omp.reportgenerator.reporter.PdfReporter;
import de.fraunhofer.fit.omp.reportgenerator.reporter.QuillTextStylingTransformer;
import de.fraunhofer.fit.omp.reportgenerator.reporter.Reporter;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.SchemaEmbedder;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.SchemaEmbedderImpl;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.TextStylingTransformerRegistry;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldMetadata;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 07.11.2017
 */
public class ReportService implements TemplateFinder {

    private static final String QUILL_SYNTAX_KIND = "quill";
    private final XDocReportRegistry registry;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    @Getter(AccessLevel.PACKAGE)
    private final Map<ReportType, Reporter> reporterMap = new HashMap<>();

    public ReportService(ApplicationConfig config) {
        registry = XDocReportRegistry.getRegistry();
        TextStylingTransformerRegistry.getRegistry().registerExternalInstance(QUILL_SYNTAX_KIND, QuillTextStylingTransformer.INSTANCE);
        ModelConverter modelConverter = new JsonDataConverter3();
        SchemaEmbedder schemaEmbedder = new SchemaEmbedderImpl();

        try {
            String template = config.getReportingDocxTemplate();
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(template)) {
                loadTemplate(in, template);
            }

            List<Reporter> reporters = Arrays.asList(
                    new DocxReporter(modelConverter, schemaEmbedder, this),
                    new PdfReporter(modelConverter, schemaEmbedder, this)
            );

            if (config.isReportingCacheReports()) {
                reporters.forEach(r -> reporterMap.put(r.getType(), new CachingReporter(r)));
            } else {
                reporters.forEach(r -> reporterMap.put(r.getType(), r));
            }
        } catch (IOException | XDocReportException e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() {
        reporterMap.values().forEach(Reporter::shutDown);
        registry.dispose();
    }

    @Override
    public IXDocReport getTemplate(String templateId) {
        r.lock();
        try {
            return registry.getReport(templateId);
        } finally {
            r.unlock();
        }
    }

    @Override
    public void loadTemplate(InputStream in, String templateId) throws IOException, XDocReportException {
        w.lock();
        try {
            // cacheReport=false, because it does not force overwriting an existing report with the same reportId,
            // but throws an exception in this case.
            IXDocReport docxReport = registry.loadReport(in, templateId, TemplateEngineKind.Velocity, false);
            // now, use the force and register.
            registry.registerReport(docxReport, true);

            setFieldsMetadata(docxReport);
            docxReport.setCacheOriginalDocument(true);
            docxReport.preprocess();

            // templates have changed -> clear cached reports that might have used older templates
            for (Reporter reporter : reporterMap.values()) {
                if (reporter instanceof CachingReporter) {
                    ((CachingReporter) reporter).invalidateCache();
                }
            }
        } finally {
            w.unlock();
        }
    }

    public ReportWrapper report(ReportType type, String templateId, String model) throws Exception {
        return findReporter(type).report(templateId, model);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Reporter findReporter(ReportType type) {
        Reporter r = reporterMap.get(type);
        if (r == null) {
            throw new RuntimeException("Reporter for '" + type.contentType + "' not found");
        }
        return r;
    }

    private static void setFieldsMetadata(IXDocReport report) {
        FieldsMetadata metadata = report.createFieldsMetadata();
        List<String> htmlFields = createHtmlDocFields("project", "service", "sequence", "function");
        for (String htmlField : htmlFields) {
            final FieldMetadata fieldMetadata = metadata.addFieldAsTextStyling(htmlField, QUILL_SYNTAX_KIND);
            fieldMetadata.setReplaceParagraphs(true);
        }
    }

    private static List<String> createHtmlDocFields(String... docContainers) {
        List<String> list = new ArrayList<>();
        for (String docContainer : docContainers) {
            list.add(docContainer + ".documentation.english");
            list.add(docContainer + ".documentation.german");
        }
        return list;
    }
}
