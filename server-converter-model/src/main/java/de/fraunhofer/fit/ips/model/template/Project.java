package de.fraunhofer.fit.ips.model.template;

import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;
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
public class Project implements StructureBase {
    @Nonnull final String title;
    @Nonnull final List<StructureBase> children;
    @Nonnull final List<Service> services;

    @Override
    public void accept(StructureVisitor visitor) {
        visitor.visit(this);
    }
}
