package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.poi;

import de.fraunhofer.fit.ips.reportgenerator.reporter.Config;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Documentations;
import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Schema;

import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class Constants {
    public static final double TWIPS_PER_CM = 566.928;
    public static final char ZERO_WIDTH_SPACE = '\u200B';
    public static final BigInteger BULLET_LIST = BigInteger.valueOf(15);
    public static final BigInteger NUMBERED_LIST = BigInteger.valueOf(27);
    public static final BigInteger SILENT_LIST = BigInteger.ZERO;
    // any uppercase letter: \p{Lu}
    // Any letter except an uppercase letter : [\p{L}&&[^\p{Lu}]]
    private static final Pattern PATTERN = Pattern.compile("(\\p{Lu}[\\p{L}&&[^\\p{Lu}]]*)");

    public static BigInteger fontSizeToHpsMeasure(final long fontSize) {
        return BigInteger.valueOf(2 * fontSize);
    }

    public static String improveCamelCaseLineBreaks(final String string) {
        Matcher matcher = PATTERN.matcher(string);
        final StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, ZERO_WIDTH_SPACE + "$0");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String getPrefixedName(final Schema schema, final QName name) {
        final String prefix = schema.getPrefix(name.getNamespaceURI());
        if (prefix.isEmpty()) {
            return name.getLocalPart();
        }
        return prefix + ":" + name.getLocalPart();
    }

    public static List<String> getDocs(final Documentations documentations) {
        return documentations.getContent(Config.PRIMARY_LANGUAGE, "");
    }

    public static List<String> getDocs(final Documentations documentations,
                                       final Documentations fallbackDocumentations) {
        return documentations.getContent(Config.PRIMARY_LANGUAGE, "", () -> getDocs(fallbackDocumentations));
    }
}
