package de.fraunhofer.fit.ips.model.template;

import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Builder
public class Assertion implements StructureBase {
    @Nonnull final String test;
    @Nullable final String xpathDefaultNamespace;
    @Nonnull final MultilingualPlaintext headingTitle;
    @Nonnull final MultilingualRichtext description;
    final boolean suppressNumbering;

    @Override
    public void accept(final StructureVisitor visitor) {
        visitor.visit(this);
    }
}
