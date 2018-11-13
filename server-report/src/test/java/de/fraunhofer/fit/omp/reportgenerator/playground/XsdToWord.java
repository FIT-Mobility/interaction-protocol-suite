package de.fraunhofer.fit.omp.reportgenerator.playground;

import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.SchemaEmbedderImpl;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.parser.XSDParser;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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
    private static final SchemaEmbedderImpl SCHEMA_EMBEDDER = new SchemaEmbedderImpl();

    public static void main(String[] args) throws IOException {
        final Path dir = Paths.get(DIR_NAME);
        try (final Stream<Path> xsds = Files.walk(dir)) {
            for (final Path xsd : (Iterable<Path>) xsds::iterator) {
                if (!xsd.toFile().isFile() || !xsd.toString().toLowerCase().endsWith(".xsd")) {
                    continue;
                }
                final byte[] docx = Files.readAllBytes(Paths.get("src/main/resources/DocumentationTemplate2.docx"));
                final Schema schema = XSDParser.createFromUri(xsd.toString()).process((a, b) -> Collections.emptyMap());
                final XWPFDocument xwpfDocument = SCHEMA_EMBEDDER.processAndReturnPOI(schema, docx);
                final String output = Paths.get("target").resolve(dir.relativize(xsd)).toString();
                final File docxFile = new File(output.substring(0, output.length() - ".xsd".length()) + ".docx");
                Files.createDirectories(docxFile.getParentFile().toPath());
                try (final FileOutputStream stream = new FileOutputStream(docxFile)) {
                    xwpfDocument.write(stream);
                }
            }
        }
    }
}
