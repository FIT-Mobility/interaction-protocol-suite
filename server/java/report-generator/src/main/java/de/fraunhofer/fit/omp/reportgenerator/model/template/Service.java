package de.fraunhofer.fit.omp.reportgenerator.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Builder
public class Service {
    @NonNull final String name;
    @NonNull final Documentation documentation;
    @NonNull final List<Function> functions;
    @NonNull final List<Sequence> sequences;

    public static Service convert(final de.fraunhofer.fit.omp.model.json.Service from,
                                  final java.util.function.Function<String, Function> functionGetter,
                                  final java.util.function.Function<String, Sequence> sequenceGetter) {
        return Service.builder()
                      .name(from.getName())
                      .documentation(Documentation.convert(from.getDocumentation()))
                      .functions(from.getFunctions().stream().map(functionGetter).collect(Collectors.toList()))
                      .sequences(from.getSequences().stream().map(sequenceGetter).collect(Collectors.toList()))
                      .build();
    }
}
