package de.fraunhofer.fit.omp.reportgenerator;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Replaces the line breaks in Throwable stack traces with custom character (and therefore condensing error logging to
 * one line), because Docker logging drivers don't support multi-line logs.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.02.2018
 */
@Plugin(name = "exTo1", category = PatternConverter.CATEGORY)
@ConverterKeys({"exTo1"})
public class ThrowableLinebreakConverter extends LogEventPatternConverter {

    private static final String REPLACEMENT = " \u27f6 ";
    private static final String CAUSE_PREFIX = " \u25CF ";
    private static final String CAUSE_CAPTION = "Caused by: ";

    private final List<PatternFormatter> formatters;

    private ThrowableLinebreakConverter(List<PatternFormatter> formatters) {
        super("exTo1", "exTo1");
        this.formatters = formatters;
    }

    public static ThrowableLinebreakConverter newInstance(final Configuration config, final String[] options) {
        if (options.length < 1) {
            LOGGER.error("Incorrect number of options. Expected at least 1, received " + options.length);
            return null;
        }

        PatternParser parser = PatternLayout.createPatternParser(config);
        List<PatternFormatter> formatters = parser.parse(options[0]);
        return new ThrowableLinebreakConverter(formatters);
    }

    @Override
    public boolean handlesThrowable() {
        return true;
    }

    @Override
    public void format(LogEvent event, StringBuilder sb) {
        doUsualFormatting(event, sb, formatters);
        doThrowableFormatting(event, sb);
    }

    private static void doUsualFormatting(LogEvent event, StringBuilder sb, List<PatternFormatter> formatters) {
        for (PatternFormatter formatter : formatters) {
            formatter.format(event, sb);
        }
    }

    private static void doThrowableFormatting(LogEvent event, StringBuilder sb) {
        Throwable t = event.getThrown();
        if (t != null) {
            sb.append(" [")
              .append(CAUSE_PREFIX)
              .append(getCustomStackTrace(t))
              .append("]");
        }
    }

    private static String getCustomStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new NewLineSuppressingWriter(sw);

        t.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }

    private static class NewLineSuppressingWriter extends PrintWriter {

        /**
         * The characters we want to escape are used in {@link Throwable#printStackTrace(Throwable.PrintStreamOrWriter)}
         */
        private final Pattern pattern = Pattern.compile("(\t)+(at )?");

        NewLineSuppressingWriter(Writer out) {
            super(out);
        }

        @Override
        public void println(Object o) {
            String s = String.valueOf(o);
            if (s.startsWith(CAUSE_CAPTION)) {
                s = CAUSE_PREFIX + s;
            } else {
                s = pattern.matcher(s).replaceFirst(REPLACEMENT);
            }
            super.print(s);
        }
    }
}
