package de.fraunhofer.fit.omp.reportgenerator.model.template;

import de.fraunhofer.fit.omp.reportgenerator.converter.NestedListFixer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Slf4j
@Builder
public class Documentation {
    @NonNull final String english;
    @NonNull final String german;

    public static Documentation convert(final de.fraunhofer.fit.omp.model.json.Documentation from) {
        return Documentation.builder()
                            .english(fixLists(from.getEnglish()))
                            .german(fixLists(from.getGerman()))
                            .build();
    }

    private static final String PSEUDO_ROOT_START = "<omp>";
    private static final String PSEUDO_ROOT_END = "</omp>";

    private static final Pattern LIST_PATTERN = Pattern.compile("(.*)(<ol>|<ul>)(.*)");


    private static String fixLists(final String richText) {
        boolean listFound = LIST_PATTERN.matcher(richText).matches();
        if (!listFound) {
            return richText;
        }

        final String prepared = PSEUDO_ROOT_START + richText + PSEUDO_ROOT_END;
        try {
            final String fixed = NestedListFixer.fixQuillIndentation(prepared);
            return fixed.substring(PSEUDO_ROOT_START.length(), fixed.length() - PSEUDO_ROOT_END.length());
        } catch (final Throwable e) {
            log.warn(e.getMessage(), e);
            return richText;
        }
    }
}
