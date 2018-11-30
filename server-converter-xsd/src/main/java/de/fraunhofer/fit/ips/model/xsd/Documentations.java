package de.fraunhofer.fit.ips.model.xsd;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public @Nullable
    List<String> getContent(final String language) {
        final List<String> stringList = docs.get(language);
        if (null != stringList) {
            return stringList;
        }
        if (docs.isEmpty()) {
            return null;
        }
        return docs.values().iterator().next();
    }
}
