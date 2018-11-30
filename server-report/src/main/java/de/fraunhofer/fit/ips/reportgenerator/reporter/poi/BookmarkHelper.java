package de.fraunhofer.fit.ips.reportgenerator.reporter.poi;

import lombok.RequiredArgsConstructor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class BookmarkHelper {
    final String hrName;

    public String getOriginalName() {
        return Constants.improveCamelCaseLineBreaks(hrName);
    }

    private String toXmlName() {
        // TODO do we need to modify the original string for it to be a valid ST_String?
        return hrName;
    }

    public String toConceptLabel() {
        return toXmlName() + "_txt";
    }

    public String toFloatLabel() {
        return toXmlName() + "_tbl";
    }
}
