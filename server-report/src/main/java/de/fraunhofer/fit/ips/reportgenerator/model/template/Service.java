package de.fraunhofer.fit.ips.reportgenerator.model.template;

import de.fraunhofer.fit.ips.reportgenerator.model.template.helper.InnerNode;
import de.fraunhofer.fit.ips.reportgenerator.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.reportgenerator.model.template.helper.StructureVisitor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Builder
public class Service implements InnerNode {
    @Nonnull final String name;
    @Nonnull final MultilingualPlaintext headingTitle;
    @Nonnull final List<Function> functions;
    @Nonnull final List<StructureBase> children;

    @Override
    public void accept(final StructureVisitor visitor) {
        visitor.visit(this);
    }
}
