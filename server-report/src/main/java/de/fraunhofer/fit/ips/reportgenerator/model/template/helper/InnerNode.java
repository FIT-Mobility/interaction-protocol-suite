package de.fraunhofer.fit.ips.reportgenerator.model.template.helper;

import de.fraunhofer.fit.ips.reportgenerator.model.template.MultilingualPlaintext;

import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface InnerNode extends StructureBase {
    List<StructureBase> getChildren();
    MultilingualPlaintext getHeadingTitle();
}
