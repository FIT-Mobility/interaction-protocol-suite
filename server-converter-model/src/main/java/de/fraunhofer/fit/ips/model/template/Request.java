package de.fraunhofer.fit.ips.model.template;

import de.fraunhofer.fit.ips.model.template.helper.InnerNode;
import de.fraunhofer.fit.ips.model.template.helper.RequestOrResponse;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Builder
public class Request implements InnerNode, RequestOrResponse {
    @Nullable final MultilingualPlaintext headingTitle;
    @Nullable final QName datatype;
    @Nonnull final List<StructureBase> children;

    @Override
    public void accept(final StructureVisitor visitor) {
        visitor.visit(this);
    }
}
