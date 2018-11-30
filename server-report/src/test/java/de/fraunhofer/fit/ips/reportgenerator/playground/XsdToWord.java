package de.fraunhofer.fit.ips.reportgenerator.playground;

import de.fraunhofer.fit.ips.model.parser.XSDParser;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import de.fraunhofer.fit.ips.reportgenerator.reporter.ReportConfiguration;
import de.fraunhofer.fit.ips.reportgenerator.reporter.ReportMetadata;
import de.fraunhofer.fit.ips.reportgenerator.reporter.Reporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class XsdToWord {
    // private static final String FILENAME = "/Users/ohler/fit/Projekte/FuH/fit-git/XSDs/FuH/stop_request.xsd";
//    private static final String FILENAME = "/Users/ohler/fit/Projekte/FuH/fit-git/XSDs/NeTEx-master-f5a5ff0/xsd/netex_part_2/part2_journeyTimes/netex_datedPassingTimes_version-v1.1.xsd";
    private static final String FILENAME = "/Users/ohler/fit/Projekte/FuH/fit-git/XSDs/NeTEx-master-f5a5ff0/xsd/NX.xsd";

    private static final String DIR_NAME = "/Users/ohler/fit/Projekte/FuH/fit-git/XSDs/FuH/";
//    private static final String DIR_NAME = "/Users/ohler/fit/Projekte/OMP/toolsupport-git/server/report-generator/src/test/resources/xsd/test.xsd";

    public static void main(String[] args) throws IOException {
        final Path dir = Paths.get(DIR_NAME);
        try (final Stream<Path> xsds = Files.walk(dir)) {
            for (final Path xsd : (Iterable<Path>) xsds::iterator) {
                if (!xsd.toFile().isFile() || !xsd.toString().toLowerCase().endsWith(".xsd")) {
                    continue;
                }
                final byte[] template = Files.readAllBytes(Paths.get("src/main/resources/DocumentationTemplate2.docx"));
                final ReportConfiguration reportConfiguration = ReportConfiguration.builder().build();
                final Schema schema = XSDParser.createFromUri(xsd.toString(), reportConfiguration.getXsdPrefix())
                                               .process(reportConfiguration.getLocalPrefixIfMissing());
                final byte[] resultDocx = Reporter.createReport(schema, Project.builder().build(), reportConfiguration, ReportMetadata.builder().build(), template);
                final String output = Paths.get("target").resolve(dir.relativize(xsd)).toString();
                final File docxFile = new File(output.substring(0, output.length() - ".xsd".length()) + ".docx");
                Files.createDirectories(docxFile.getParentFile().toPath());
                try (final FileOutputStream stream = new FileOutputStream(docxFile)) {
                    stream.write(resultDocx);
                }
            }
        }
    }
}
