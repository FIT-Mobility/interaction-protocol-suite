package de.fraunhofer.fit.omp.testmonitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.fit.omp.model.json.OmpToolProjectSchema;
import de.fraunhofer.fit.omp.model.json.Service;
import de.fraunhofer.fit.omp.testmonitor.data.DataExtractor;
import de.fraunhofer.fit.omp.testmonitor.data.FunctionData;
import de.fraunhofer.fit.omp.testmonitor.routing.messagebased.MessageBasedFunctionInfo;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class ParserTester {
    private static final String FILE = "../report-generator/src/test/resources/sampleJSON2.json";

    public static void main(String[] args) throws IOException, TransformerException, ParserConfigurationException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final OmpToolProjectSchema ompToolProjectSchema = objectMapper.readValue(new File(FILE), OmpToolProjectSchema.class);
        final Map<String, String> function2Service = new HashMap<>();
        for (final Service service : ompToolProjectSchema.getServices()) {
            for (final String function : service.getFunctions()) {
                function2Service.put(function, service.getName());
            }
        }
        final HashMap<QName, MessageBasedFunctionInfo> lookup = new HashMap<>();
        final String functionSchema = DataExtractor.generateFunctionSchema(
                new StreamSource(Paths.get("/Users/ohler/fit/Projekte/FuH/fit-git/XSDs/OMP/data-type-schema.xsd").toFile()),
                ompToolProjectSchema.getFunctions().stream().map(FunctionData::fromJSON).collect(Collectors.toList()),
                function2Service::get,
                fi -> lookup.put(fi.getRequestElementName(), fi));
        for (final Map.Entry<QName, MessageBasedFunctionInfo> entry : lookup.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
