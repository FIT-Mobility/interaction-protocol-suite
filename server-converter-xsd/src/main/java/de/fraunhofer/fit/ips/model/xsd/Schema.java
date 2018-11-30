package de.fraunhofer.fit.ips.model.xsd;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 15.12.2017
 */
@ToString
@Slf4j
public class Schema {
    @Getter
    private final String xsdPath;

    @Getter
    private final Map<QName, NamedConceptWithOrigin> concepts;

    @Getter
    private final Set<QName> internalConceptNames;

    private final Map<String, String> namespace2Prefix;

    final AtomicInteger nsCounter = new AtomicInteger(1);

    public Schema(final String xsdPath, final Map<String, String> namespace2Prefix,
                  final Map<QName, NamedConceptWithOrigin> concepts) {
        this.xsdPath = xsdPath;
        this.namespace2Prefix = namespace2Prefix;
        this.concepts = concepts;
        this.internalConceptNames
                = concepts.values().stream()
                          .filter(ct -> ct.getOrigin().isInternal())
                          .map(NamedConceptWithOrigin::getName)
                          .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public @Nullable
    NamedConceptWithOrigin getConcept(final QName name) {
        return concepts.get(name);
    }

    public String getPrefix(final String namespace) {
        return namespace2Prefix.computeIfAbsent(namespace, ns -> {
            final String prefix = "ns" + nsCounter.getAndIncrement();
            log.warn("Could not find a prefix for {}, using {}!", ns, prefix);
            return prefix;
        });
    }
}
