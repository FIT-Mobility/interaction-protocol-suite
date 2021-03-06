package de.fraunhofer.fit.ips.reportgenerator.reporter.poi;

import de.fraunhofer.fit.ips.model.xsd.Documentations;
import de.fraunhofer.fit.ips.model.xsd.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.Collections;
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

    public static @Nonnull
    List<String> getDocs(final Context context, final Documentations documentations) {
        final List<String> docs = getDocs(context.getReportConfiguration().getXsdDocumentationLanguage(), documentations);
        if (null == docs) {
            return Collections.emptyList();
        }
        return docs;
    }

    public static @Nullable
    List<String> getDocs(final String language, final Documentations documentations) {
        final List<String> content = documentations.getContent(language);
        if (null != content) {
            return content;
        }
        return documentations.getContent("");
    }

    public static @Nullable
    List<String> getDocs(final Context context, final Documentations documentations,
                         final Documentations fallbackDocumentations) {
        return getDocs(context.getReportConfiguration().getXsdDocumentationLanguage(), documentations, fallbackDocumentations);
    }

    public static @Nullable
    List<String> getDocs(final String language,
                         final Documentations documentations,
                         final Documentations fallbackDocumentations) {
        final List<String> docs = getDocs(language, documentations);
        if (docs != null) {
            return docs;
        }
        return getDocs(language, fallbackDocumentations);
    }
}
