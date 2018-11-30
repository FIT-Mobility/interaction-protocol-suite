package de.fraunhofer.fit.ips.reportgenerator.reporter.poi;

import lombok.RequiredArgsConstructor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class BookmarkHelper {
    // identifier may be UUID (36 characters), so prefixes such as txt_ and tbl_ are ok, but longer ones would go beyond the 40 character limit of word
    final String identifier;
    final String hrName;

    public BookmarkHelper(final String hrName) {
        this.identifier = hrName;
        this.hrName = hrName;
    }

    public String getOriginalName() {
        return Constants.improveCamelCaseLineBreaks(hrName);
    }

    private String toXmlName() {
        // TODO do we need to modify the original string for it to be a valid ST_String?
        return identifier;
    }

    public String toConceptLabel() {
        return "txt_" + toXmlName();
    }

    public String toFloatLabel() {
        return "tbl_" + toXmlName();
    }
}
