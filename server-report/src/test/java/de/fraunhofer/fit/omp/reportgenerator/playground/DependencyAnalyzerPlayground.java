package de.fraunhofer.fit.omp.reportgenerator.playground;

import de.fraunhofer.fit.omp.reportgenerator.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.DependencyAnalyzer;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.parser.XSDParser;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class DependencyAnalyzerPlayground {
    public static void main(final String[] args) {
        final XSDParser xsdParser = XSDParser.createFromUri("src/test/resources/xsd/IBIS-IP_PassengerCountingService_V1.0.xsd");
        final Schema schema = xsdParser.process((a, b) -> new HashMap<>());

        final List<QName> localDataTypes =
                schema.getConcepts().values()
                      .stream()
                      .filter(ct -> ct.getOrigin().isInternal())
                      .map(NamedConceptWithOrigin::getName)
                      .collect(Collectors.toList());

        final HashMap<QName, LinkedHashSet<QName>> rrtToChildren = DependencyAnalyzer.analyze(schema, localDataTypes);

        log.info("DEPENDENCIES:");

        for (Map.Entry<QName, LinkedHashSet<QName>> entry : rrtToChildren.entrySet()) {
            log.info(entry.getKey().getLocalPart());
            for (QName child : entry.getValue()) {
                log.info("\t{}", child);
            }
        }
    }
}
