package de.fraunhofer.fit.omp.reportgenerator.model.xsd;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 18.01.2018
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@Getter
public class Documentations {
    public static final Documentations EMPTY = new Documentations(Collections.emptyMap());

    // (language, contents)
    private final Map<String, List<String>> docs;

    public Documentations() {
        this(new HashMap<>());
    }

    public List<String> getContent(String primaryLanguage, String fallbackLanguage) {
        return getContent(primaryLanguage, fallbackLanguage, Collections::emptyList);
    }

    public List<String> getContent(String primaryLanguage, String fallbackLanguage,
                                   Supplier<List<String>> defaultSupplier) {
        List<String> content = docs.get(primaryLanguage);
        if (content != null) {
            return content;
        }
        return docs.getOrDefault(fallbackLanguage, defaultSupplier.get());
    }
}
