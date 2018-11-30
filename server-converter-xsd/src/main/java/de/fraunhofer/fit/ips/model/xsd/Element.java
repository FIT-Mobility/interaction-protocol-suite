package de.fraunhofer.fit.ips.model.xsd;

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
public class Element implements NamedConceptWithOrigin {
    private final QName name;
    private final QName dataType;
    private final String cardinality;
    private final Origin origin;
    private final Documentations docs;

    @Override
    public void accept(final NamedConceptWithOriginVisitor visitor) {
        visitor.visit(this);
    }
}
