package de.fraunhofer.fit.ips.reportgenerator.model.xsd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * A wrapper for subsequent xs:element declarations
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.01.2018
 */
@RequiredArgsConstructor
@ToString
@Getter
public class ElementList implements SequenceOrChoiceOrGroupRefOrElementList {
    private final List<Element> elements;

    @Override
    public void accept(final SequenceOrChoiceOrGroupRefOrElementListVisitor visitor) {
        visitor.visit(this);
    }
}
