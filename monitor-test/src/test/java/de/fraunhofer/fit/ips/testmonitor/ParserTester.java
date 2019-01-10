package de.fraunhofer.fit.ips.testmonitor;

import de.fraunhofer.fit.ips.model.SimpleConverter;
import de.fraunhofer.fit.ips.model.IllegalDocumentStructureException;
import de.fraunhofer.fit.ips.model.simple.Project;
import de.fraunhofer.fit.ips.proto.javabackend.CreateReportRequest;
import de.fraunhofer.fit.ips.testmonitor.data.DataExtractor;
import de.fraunhofer.fit.ips.testmonitor.routing.messagebased.MessageBasedFunctionInfo;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class ParserTester {
    // TODO create files
    private static final String SERIALIZED_CREATE_REPORT_REQUEST = "path/to/create-report-request.ser";
    private static final String DATA_TYPE_SCHEMA_FILE = "path/to/data-type-schema.xsd";

    public static void main(String[] args)
            throws IOException, TransformerException, ParserConfigurationException, IllegalDocumentStructureException {
        final Project project;
        try (final FileInputStream fileInputStream = new FileInputStream(SERIALIZED_CREATE_REPORT_REQUEST)) {
            project = SimpleConverter.convert(CreateReportRequest.parseFrom(fileInputStream).getSchemaAndProjectStructure().getProject());
        }
        final HashMap<QName, MessageBasedFunctionInfo> lookup = new HashMap<>();
        final String functionSchema = DataExtractor.generateFunctionSchema(
                new StreamSource(Paths.get(DATA_TYPE_SCHEMA_FILE).toFile()),
                project, fi -> lookup.put(fi.getRequestElementName(), fi));
        for (final Map.Entry<QName, MessageBasedFunctionInfo> entry : lookup.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
