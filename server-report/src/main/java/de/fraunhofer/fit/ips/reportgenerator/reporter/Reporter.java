package de.fraunhofer.fit.ips.reportgenerator.reporter;

import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.TextStylingTransformerRegistry;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldMetadata;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import lombok.Data;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.xml.namespace.QName;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Reporter {
    private static final String QUILL_SYNTAX_KIND = "quill";

    static {
        TextStylingTransformerRegistry.getRegistry().registerExternalInstance(QUILL_SYNTAX_KIND, QuillTextStylingTransformer.INSTANCE);
    }

    public static byte[] createReport(final Schema schema,
                                      final Project project,
                                      final ReportConfiguration reportConfiguration,
                                      final ReportMetadata reportMetadata,
                                      final byte[] template) {
        // identify assigned particles, unassigned particles have to be printed without the bookmarking magic
        final Set<QName> assignedParticles = AssignedParticlesGatherer.gather(project);

        // put template into apache poi
        // create all the stuff in the document according to structure
        // leave placeholders for rich-text and gather field names into map within context
        final PreparationResult result = process(template, document -> StructureEmbedder.embed(schema, project, reportConfiguration, assignedParticles, document));

        // put current document state into XDocReport
        // mark all the markers as QUILL
        // let it run
        return runXDocReporter(result, contextMap -> injectMetadata(project, reportMetadata, contextMap));
    }

    private static void injectMetadata(final Project project,
                                       final ReportMetadata reportMetadata,
                                       final Map<String, Object> contextMap) {
        // FIXME insert metadata information into context map, e.g. project title, authors, ...
        // has to be in sync with documentation template
        contextMap.put("project_title", project.getTitle());
        contextMap.put("authors", Collections.emptyList());
    }

    private static byte[] runXDocReporter(final PreparationResult result,
                                          final Consumer<Map<String, Object>> metadataInjector) {
        try {
            final IXDocReport report = XDocReportRegistry.getRegistry().loadReport(new ByteArrayInputStream(result.docx), TemplateEngineKind.Freemarker);
            setFieldsMetadata(report, result.richtextMarkerManager);
            final Map<String, Object> contextMap = result.richtextMarkerManager.contextMap;
            metadataInjector.accept(contextMap);
            try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                report.process(contextMap, outputStream);
                return outputStream.toByteArray();
            }
        } catch (final IOException | XDocReportException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setFieldsMetadata(final IXDocReport report, final RichtextMarkerManager richtextMarkerManager) {
        final FieldsMetadata metadata = report.createFieldsMetadata();
        for (final String marker : richtextMarkerManager.contextMap.keySet()) {
            final FieldMetadata fieldMetadata = metadata.addFieldAsTextStyling(marker, QUILL_SYNTAX_KIND);
            fieldMetadata.setReplaceParagraphs(true);
        }
    }

    @Data
    static class PreparationResult {
        final RichtextMarkerManager richtextMarkerManager;
        final byte[] docx;
    }

    public static class RichtextMarkerManager {
        final Map<String, Object> contextMap = new HashMap<>();

        public String newMarkerForRichtext(final String richtext) {
            final String marker = "marker_" + UUID.randomUUID().toString().replace("-", "_");
            contextMap.put(marker, richtext);
            return marker;
        }
    }

    private static XWPFDocument createDocument(final byte[] docx) {
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(docx)) {
            return new XWPFDocument(bis);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static PreparationResult process(final byte[] template,
                                             final Function<XWPFDocument, RichtextMarkerManager> handler) {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final BufferedOutputStream stream = new BufferedOutputStream(bos);
             final XWPFDocument xwpfDocument = createDocument(template)) {
            final RichtextMarkerManager richtextMarkerManager = handler.apply(xwpfDocument);
            xwpfDocument.write(stream);
            return new PreparationResult(richtextMarkerManager, bos.toByteArray());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
