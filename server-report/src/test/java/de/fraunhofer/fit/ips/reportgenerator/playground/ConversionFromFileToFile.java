package de.fraunhofer.fit.ips.reportgenerator.playground;

import de.fraunhofer.fit.ips.reportgenerator.ApplicationConfig;
import de.fraunhofer.fit.ips.reportgenerator.ReportType;
import de.fraunhofer.fit.ips.reportgenerator.service.ReportService;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 07.11.2017
 */
public class ConversionFromFileToFile {

    public static void main(String[] args) throws Exception {
        ApplicationConfig config = ApplicationConfig.builder().build();

        ReportService rs = new ReportService(config);
        String templateId = config.getReportingDocxTemplate();

        try {
            String json = new String(
                    Files.readAllBytes(
                            Paths.get(ConversionFromFileToFile.class
                                    .getClassLoader()
                                    .getResource("sampleJSON2.json")
                                    .toURI())
                    ), StandardCharsets.UTF_8.name()
            );

            // to docx
            try (FileOutputStream outputStream = new FileOutputStream("target/sample.docx")) {
                outputStream.write(rs.report(ReportType.DOCX, templateId, json).report);
            }

            // to pdf
            try (FileOutputStream outputStream = new FileOutputStream("target/sample.pdf")) {
                outputStream.write(rs.report(ReportType.PDF, templateId, json).report);
            }
        } finally {
            rs.destroy();
        }
    }

}
