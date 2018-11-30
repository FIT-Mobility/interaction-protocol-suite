package de.fraunhofer.fit.ips.reportgenerator.reporter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import de.fraunhofer.fit.ips.reportgenerator.Utils;
import de.fraunhofer.fit.ips.reportgenerator.reporter.temp.PdfConverter2;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.itext.extension.font.AbstractFontRegistry;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class PdfReporter {
    private final IFontProvider defaultFontProvider = CachingFontRegistry.INSTANCE;

    // if some wanted font (key) is not found, we replace it with a graceful fallback (value) that is bundled
    // with the application. use case: proprietary font used in template -> open-source free alternative
    private final Map<String, String> fontMapper = new HashMap<>();
    private final FontErrorCollectingRegistry fontProvider = new FontErrorCollectingRegistry(defaultFontProvider, fontMapper);

    public PdfReporter() {
        fontMapper.put("Arial", "Arimo");
        fontMapper.put("Calibri", "Carlito");
        fontMapper.put("Courier New", "Cousine");
        fontMapper.put("Times New Roman", "Tinos");
    }

    public byte[] report(byte[] docx) throws IOException {
        try (final XWPFDocument docxWithSchema = new XWPFDocument(new ByteArrayInputStream(docx));
             final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PdfConverter2.getInstance().convert(docxWithSchema, bos, PdfOptions.create().fontProvider(fontProvider));
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
