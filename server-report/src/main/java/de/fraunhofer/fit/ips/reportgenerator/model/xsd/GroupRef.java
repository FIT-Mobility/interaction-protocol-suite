package de.fraunhofer.fit.ips.reportgenerator.model.xsd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.xml.namespace.QName;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.01.2018
 */
@RequiredArgsConstructor
@ToString
@Getter
public class GroupRef implements SequenceOrChoiceOrGroupRef {
    private final QName refName;
    private final String cardinality;
    private final Documentations docs;

    @Override
    public void accept(final SequenceOrChoiceOrGroupRefOrElementListVisitor visitor) {
        visitor.visit(this);
    }
}
