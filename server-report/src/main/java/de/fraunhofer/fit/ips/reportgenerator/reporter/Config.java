package de.fraunhofer.fit.ips.reportgenerator.reporter;

import lombok.experimental.UtilityClass;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@UtilityClass
public class Config {
    public static final boolean EXPAND_ATTRIBUTE_GROUPS = true;
    public static final boolean EXPAND_ELEMENT_GROUPS = true;
    public static final boolean HIDE_INHERITANCE_IN_EXTENSIONS = true;
    public static final boolean INLINE_ENUMS = true;
    public static final String XSD_PREFIX = "xs";
    public static final String LOCAL_PREFIX_IF_MISSING = "tns";
    public static final String PRIMARY_LANGUAGE = "en";
}
