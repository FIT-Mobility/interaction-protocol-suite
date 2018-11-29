package de.fraunhofer.fit.ips.reportgenerator.reporter;

import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.reportgenerator.ApplicationConfig;
import de.fraunhofer.fit.ips.reportgenerator.converter.ModelConverter;
import de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.SchemaEmbedder;
import de.fraunhofer.fit.ips.reportgenerator.ReportType;
import de.fraunhofer.fit.ips.reportgenerator.model.ReportContext;
import de.fraunhofer.fit.ips.reportgenerator.model.ReportWrapper;
import de.fraunhofer.fit.ips.reportgenerator.service.TemplateFinder;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.template.IContext;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.11.2017
 */
@RequiredArgsConstructor
public class DocxReporter implements Reporter {

    private final ModelConverter modelConverter;
    private final SchemaEmbedder schemaEmbedder;
    private final TemplateFinder templateFinder;

    @Override
    public ReportType getType() {
        return ReportType.DOCX;
    }

    @Override
    public ReportWrapper report(String templateId, String str) throws Exception {
        IXDocReport template = templateFinder.getTemplate(templateId);
        ReportContext context = modelConverter.getContext(template, str);
        byte[] docx = getDocx(template, context);
        byte[] docxWithSchema = schemaEmbedder.process(context.getSchema(), docx);
        return new ReportWrapper(getType(), docxWithSchema);
    }

    private byte[] getDocx(IXDocReport template, IContext context) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            template.process(context, bos);
            return bos.toByteArray();
        }
    }
}
