package de.fraunhofer.fit.ips.reportgenerator.model.template;

import de.fraunhofer.fit.ips.reportgenerator.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.reportgenerator.model.template.helper.StructureVisitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
public class Text implements StructureBase {
    @Nonnull final MultilingualRichtext rtContent;

    @Override
    public void accept(final StructureVisitor visitor) {
        visitor.visit(this);
    }
}
