package de.fraunhofer.fit.ips.reportgenerator.model.xsd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.01.2018
 */
@RequiredArgsConstructor
@ToString
@Getter
public class Choice implements SequenceOrChoice {
    private final String cardinality;
    private final Documentations docs;
    private final List<SequenceOrChoiceOrGroupRefOrElementList> particleList;

    @Override
    public void accept(final SequenceOrChoiceOrGroupRefOrElementListVisitor visitor) {
        visitor.visit(this);
    }
}