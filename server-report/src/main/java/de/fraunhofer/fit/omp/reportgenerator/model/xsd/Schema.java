package de.fraunhofer.fit.omp.reportgenerator.model.xsd;

import de.fraunhofer.fit.omp.reportgenerator.model.template.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 15.12.2017
 */
@RequiredArgsConstructor
@ToString
@Slf4j
public class Schema {
    @Getter
    private final String xsdPath;

    private final Map<String, String> namespace2Prefix;

    @Getter
    private final Map<QName, NamedConceptWithOrigin> concepts;

    @Getter
    private final Map<String, Function> operations;

    final AtomicInteger nsCounter = new AtomicInteger(1);

    public String getPrefix(final String namespace) {
        return namespace2Prefix.computeIfAbsent(namespace, ns -> {
            final String prefix = "ns" + nsCounter.getAndIncrement();
            log.warn("Could not find a prefix for {}, using {}!", ns, prefix);
            return prefix;
        });
    }
}
