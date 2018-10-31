package de.fraunhofer.fit.omp.reportgenerator.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Builder
public class Project {
    @NonNull final String title;
    @NonNull final Documentation documentation;
    @NonNull final List<Service> services;

    public static Project convert(final de.fraunhofer.fit.omp.model.json.OmpToolProjectSchema from,
                                  final Map<String, Function> functionMap) {
        final de.fraunhofer.fit.omp.model.json.Project jsonProject = from.getProject();
        final Map<String, Sequence> sequenceMap = from.getSequences().stream().map(Sequence::convert).collect(toMap(Sequence::getName, identity()));
        return Project.builder()
                      .title(jsonProject.getTitle())
                      .documentation(Documentation.convert(jsonProject.getDocumentation()))
                      .services(from.getServices().stream().map(s -> Service.convert(s, functionMap::get, sequenceMap::get)).collect(toList()))
                      .build();
    }
}
