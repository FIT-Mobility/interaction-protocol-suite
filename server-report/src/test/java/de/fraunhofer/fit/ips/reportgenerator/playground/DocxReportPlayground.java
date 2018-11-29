package de.fraunhofer.fit.ips.reportgenerator.playground;

import com.google.common.base.Charsets;
import de.fraunhofer.fit.ips.reportgenerator.converter.JsonDataConverter3;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <ohler@dbis.rwth-aachen.de>
 */
public class DocxReportPlayground {
    public static void main(String[] args) {
        final XDocReportRegistry registry = XDocReportRegistry.getRegistry();
        try (final FileInputStream templateStream
                     = new FileInputStream("src/test/resources/playground-template.docx")) {
            final IXDocReport docxReport = registry.loadReport(
                    templateStream,
                    TemplateEngineKind.Velocity
            );
            final String jsonAsString =
                    Files.readAllLines(
                            Paths.get("src/test/resources/playground-template-data.json"),
                            Charsets.UTF_8)
                         .stream().collect(Collectors.joining());
            final IContext context = new JsonDataConverter3().getContext(docxReport, jsonAsString);
            
            docxReport.process(context, new FileOutputStream("src/test/resources/playground-template-instance.docx"));

            registry.dispose();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } catch (XDocReportException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
