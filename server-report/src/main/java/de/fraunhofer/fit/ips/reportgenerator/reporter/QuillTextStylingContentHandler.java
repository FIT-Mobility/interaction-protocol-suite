package de.fraunhofer.fit.ips.reportgenerator.reporter;

import com.google.common.base.Strings;
import fr.opensagres.xdocreport.document.textstyling.IDocumentHandler;
import fr.opensagres.xdocreport.document.textstyling.html.HTMLTextStylingContentHandler;
import fr.opensagres.xdocreport.document.textstyling.properties.ContainerProperties;
import fr.opensagres.xdocreport.document.textstyling.properties.PropertiesEnhancer;
import fr.opensagres.xdocreport.document.textstyling.properties.TextAlignment;
import org.xml.sax.Attributes;

import java.util.function.Supplier;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class QuillTextStylingContentHandler extends HTMLTextStylingContentHandler {

    // word uses factors of 720 as default
    private static final int INDENTATION_CONSTANT = 720;

    public QuillTextStylingContentHandler(IDocumentHandler documentHandler) {
        super(documentHandler, new QuillPropertiesEnhancer());
    }

    private static class QuillPropertiesEnhancer implements PropertiesEnhancer {
        @Override
        public <T extends ContainerProperties> T enhance(Attributes attributes, T properties, Supplier<T> supplier) {
            final String classesString = attributes.getValue("class");
            if (Strings.isNullOrEmpty(classesString)) {
                return properties;
            }

            final NullGuard<T> nullGuard = new NullGuard<>(properties, supplier);
            final String[] classes = classesString.split(" ");

            for (String elementClass : classes) {
                String trimmed = elementClass.trim();
                processAlignment(nullGuard, trimmed);
                processIndentation(nullGuard, trimmed);
            }

            return nullGuard.getCurrent();
        }

        private static <T extends ContainerProperties> void processAlignment(NullGuard<T> val, String elementClass) {
            switch (elementClass) {
                case "ql-align-center":
                    val.getNonNull().setTextAlignment(TextAlignment.Center);
                    break;
                case "ql-align-right":
                    val.getNonNull().setTextAlignment(TextAlignment.Right);
                    break;
                case "ql-align-justify":
                    val.getNonNull().setTextAlignment(TextAlignment.Justify);
                    break;
            }
        }

        private static <T extends ContainerProperties> void processIndentation(NullGuard<T> val, String elementClass) {
            String prefix = "ql-indent-";
            if (!elementClass.startsWith(prefix)) {
                return;
            }
            String indLevelString = elementClass.substring(prefix.length());
            int indLevel;
            try {
                indLevel = Integer.parseInt(indLevelString);
            } catch (NumberFormatException e) {
                // just skip this enhancement
                return;
            }
            if (indLevel > 0) {
                val.getNonNull().setIndentationLeft(INDENTATION_CONSTANT * indLevel);
            }
        }
    }

    private static class NullGuard<T> {
        private final Supplier<T> supplier;
        private T value;

        private NullGuard(final T value, final Supplier<T> supplier) {
            this.supplier = supplier;
            this.value = value;
        }

        private T getNonNull() {
            if (null == value) {
                value = supplier.get();
            }
            return value;
        }

        private T getCurrent() {
            return value;
        }
    }
}
