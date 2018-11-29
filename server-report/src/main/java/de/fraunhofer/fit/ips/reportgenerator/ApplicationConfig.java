package de.fraunhofer.fit.ips.reportgenerator;

import fr.opensagres.xdocreport.converter.ConverterRegistry;
import fr.opensagres.xdocreport.converter.IConverter;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.discovery.IConverterDiscovery;
import fr.opensagres.xdocreport.converter.docx.poi.itext.OpenXMLFormats2PDFViaITextConverter;
import fr.opensagres.xdocreport.converter.docx.poi.itext.XWPF2PDFViaITextConverter;
import fr.opensagres.xdocreport.core.io.XDocArchive;
import fr.opensagres.xdocreport.document.docx.DocxReport;
import fr.opensagres.xdocreport.template.IContext;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 28.11.2017
 */
@Getter
@ToString
@Builder
public class ApplicationConfig {

    @Builder.Default private String serverHost = "0.0.0.0";
    @Builder.Default private int serverPort = 8080;
    @Builder.Default private int serverMinThreads = 4;
    @Builder.Default private int serverMaxThreads = 50;
    @Builder.Default private int serverIdleTimeoutMillis = (int) TimeUnit.MINUTES.toMillis(1);
    @Builder.Default private boolean serverLogRequests = true;

    @Builder.Default private String apiAsyncReportPath = "/report-async";
    @Builder.Default private String apiTemplateUploadPath = "/template-upload";
    @Builder.Default private String apiValidatorPath = "/validate";

    @Builder.Default private String reportingApiTemplateIdHeader = "X-Template-Id";
    @Builder.Default private String reportingDocxTemplate = "DocumentationTemplate.docx";
    @Builder.Default private boolean reportingCacheReports = false;

    /**
     * XDocReport's internals:
     *
     * Converters implement {@link IConverter}. They are identified by their {@link IConverterDiscovery} instances and
     * discovered/registered in {@link ConverterRegistry} with classpath scanning during init. There are two main PDF
     * converters: {@link XWPF2PDFViaITextConverter} and {@link OpenXMLFormats2PDFViaITextConverter}.
     *
     * {@link XWPF2PDFViaITextConverter} has no entries support {@link XWPF2PDFViaITextConverter#canSupportEntries()}
     * and therefore the internal DOCX representation {@link XDocArchive} has to be converted to a ZIP archive and then
     * to an InputStream, which consumes too much time. See lines 705-714 in {@link DocxReport#convert(IContext,
     * Options, OutputStream)} for the decision logic.
     *
     * {@link OpenXMLFormats2PDFViaITextConverter} has entries support {@link OpenXMLFormats2PDFViaITextConverter#canSupportEntries()}
     * and therefore during the conversion the intermediate steps (ZIP archive and InputStream) can be skipped which
     * accelerates the process considerably.
     *
     * TODO: Why are there two converters? Are there any qualitative differences of the produced documents between these two converters? Investigate.
     */
    @Builder.Default private boolean reportingFastPdfConversion = true;

}
