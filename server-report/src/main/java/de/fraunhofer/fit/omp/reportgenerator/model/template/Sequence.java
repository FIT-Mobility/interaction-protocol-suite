package de.fraunhofer.fit.omp.reportgenerator.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Builder
public class Sequence {
    @NonNull final String name;

    public static Sequence convert(final de.fraunhofer.fit.omp.model.json.Sequence from) {
        return Sequence.builder()
                       .name(from.getName())
                       .build();
    }
}
