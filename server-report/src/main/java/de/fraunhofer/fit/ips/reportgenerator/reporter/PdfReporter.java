package de.fraunhofer.fit.ips.reportgenerator.reporter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import de.fraunhofer.fit.ips.reportgenerator.converter.ModelConverter;
import de.fraunhofer.fit.ips.reportgenerator.reporter.temp.PdfConverter2;
import de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.SchemaEmbedder;
import de.fraunhofer.fit.ips.reportgenerator.ReportType;
import de.fraunhofer.fit.ips.reportgenerator.Utils;
import de.fraunhofer.fit.ips.reportgenerator.model.ReportContext;
import de.fraunhofer.fit.ips.reportgenerator.model.ReportWrapper;
import de.fraunhofer.fit.ips.reportgenerator.service.TemplateFinder;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.itext.extension.font.AbstractFontRegistry;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import fr.opensagres.xdocreport.template.IContext;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.11.2017
 */
@Slf4j
public class PdfReporter implements Reporter {

    private final IFontProvider defaultFontProvider = CachingFontRegistry.INSTANCE;

    private final ModelConverter modelConverter;
    private final SchemaEmbedder schemaEmbedder;
    private final TemplateFinder templateFinder;

    // if some wanted font (key) is not found, we replace it with a graceful fallback (value) that is bundled
    // with the application. use case: proprietary font used in template -> open-source free alternative
    private final Map<String, String> fontMapper = new HashMap<>();

    public PdfReporter(ModelConverter modelConverter, SchemaEmbedder schemaEmbedder, TemplateFinder templateFinder) {
        this.modelConverter = modelConverter;
        this.schemaEmbedder = schemaEmbedder;
        this.templateFinder = templateFinder;

        fontMapper.put("Arial", "Arimo");
        fontMapper.put("Calibri", "Carlito");
        fontMapper.put("Courier New", "Cousine");
        fontMapper.put("Times New Roman", "Tinos");
    }

    @Override
    public ReportType getType() {
        return ReportType.PDF;
    }

    @Override
    public ReportWrapper report(String templateId, String str) throws Exception {
        IXDocReport template = templateFinder.getTemplate(templateId);
        ReportContext context = modelConverter.getContext(template, str);
        return handleByCollectingFontErrors(template, context);
    }

    private ReportWrapper handleByCollectingFontErrors(IXDocReport template, ReportContext context) throws Exception {
        FontErrorCollectingRegistry fontProvider = new FontErrorCollectingRegistry(defaultFontProvider, fontMapper);

        byte[] docx = getDocx(template, context);
        byte[] pdf = getPdf(context, fontProvider, docx);

        if (fontProvider.missingFonts.isEmpty()) {
            return new ReportWrapper(getType(), pdf);
        } else {
            String msg = "The fonts " + fontProvider.missingFonts.toString() + " could not be found";
            return new ReportWrapper(getType(), pdf, Collections.singletonList(msg));
        }
    }

    private byte[] getPdf(ReportContext context, FontErrorCollectingRegistry fontProvider, byte[] docx)
            throws IOException {
        try (XWPFDocument docxWithSchema = schemaEmbedder.processAndReturnPOI(context.getSchema(), docx);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PdfConverter2.getInstance().convert(docxWithSchema, bos, PdfOptions.create().fontProvider(fontProvider));
            return bos.toByteArray();
        }
    }

    private byte[] getDocx(IXDocReport template, IContext context) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            template.process(context, bos);
            return bos.toByteArray();
        }
    }

    // -------------------------------------------------------------------------
    // Private helper classes w.r.t. fonts
    // -------------------------------------------------------------------------

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class FontErrorCollectingRegistry implements IFontProvider {

        private final IFontProvider delegate;
        private final Map<String, String> fontMapper;

        private final Set<String> missingFonts = new HashSet<>();

        @Override
        public Font getFont(String familyName, String encoding, float size, int style, Color color) {
            Font found = delegate.getFont(familyName, encoding, size, style, color);
            if (isFontFound(familyName, found)) {
                return found;
            }

            // try map fallback
            String mappedFont = fontMapper.get(familyName);
            if (mappedFont == null) {
                return found; // no mapping found, return suboptimal found font anyways
            } else {
                return getFont(mappedFont, encoding, size, style, color);
            }
        }

        private boolean isFontFound(String familyName, Font found) {
            String foundFamilyName = found.getFamilyname();
            if (familyName.equals(foundFamilyName)) {
                return true; // Ok, no problems, we found what we wanted
            }

            // -------------------------------------------------------------------------
            // Handle problematic cases
            // -------------------------------------------------------------------------

            BaseFont baseFont = found.getBaseFont();
            if (baseFont == null) {
                addAndLog(familyName, foundFamilyName);
            } else {
                if (!containsWanted(familyName, baseFont.getFamilyFontName())) {
                    addAndLog(familyName, baseFont.getFamilyFontName());
                }
            }

            return false;
        }

        private void addAndLog(String wanted, Object found) {
            if (missingFonts.add(wanted)) {
                log.warn("Font problem! Wanted: {} - Found: {}", wanted, found);
            }
        }

        private static boolean containsWanted(String wanted, String[][] found) {
            for (String[] s : found) {
                String fullName = s[3];
                if (wanted.equals(fullName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A modified version of {@link AbstractFontRegistry} to register extra directories and cache fonts.
     */
    private static class CachingFontRegistry implements IFontProvider {

        static final CachingFontRegistry INSTANCE = new CachingFontRegistry();

        private final Cache<WantedFontDescription, Font> fontCache;

        /**
         * {@link AbstractFontRegistry#initFontRegistryIfNeeded()}
         */
        private CachingFontRegistry() {
            // clear built-in fonts which may clash with document fonts
            AbstractFontRegistry.ExtendedBaseFont.clearBuiltinFonts();

            Utils.registerFontPaths();

            fontCache = CacheBuilder.newBuilder()
                                    .maximumSize(250)
                                    .expireAfterAccess(1, TimeUnit.HOURS)
                                    .build();
        }

        @Override
        public Font getFont(String familyName, String encoding, float size, int style, Color color) {
            WantedFontDescription key = new WantedFontDescription(familyName, encoding, size, style, color);
            try {
                return fontCache.get(key, () -> FontFactory.getFont(familyName, encoding, size, style, color));
            } catch (ExecutionException e) {
                log.warn("Exception happened", e);
                // Cache.get(...) signature has this checked exception, even though the used method does not throw
                // any checked exceptions. Let's give the call-side the font anyways while bypassing the cache.
                return FontFactory.getFont(familyName, encoding, size, style, color);
            }
        }

        @EqualsAndHashCode
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        private static class WantedFontDescription {
            private final String familyName;
            private final String encoding;
            private final float size;
            private final int style;
            private final Color color;
        }
    }
}
