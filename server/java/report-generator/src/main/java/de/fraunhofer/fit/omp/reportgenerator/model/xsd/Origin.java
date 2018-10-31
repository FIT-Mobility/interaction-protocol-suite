package de.fraunhofer.fit.omp.reportgenerator.model.xsd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.01.2018
 */
@RequiredArgsConstructor
@ToString
@Getter
public class Origin {

    public static final Origin XML_SCHEMA_XSD = new Origin.Builtin();

    private final boolean isInternal; // is the object from the main/input XSD (true) or included/imported XSDs (false)?
    private final boolean isAnonymous;
    private final String xsdPath;

    public Origin(boolean isInternal, String xsdPath) {
        this(isInternal, false, xsdPath);
    }

    // for data types from XMLSchema.xsd
    private static class Builtin extends Origin {
        Builtin() {
            super(false, "");
        }
    }
}
