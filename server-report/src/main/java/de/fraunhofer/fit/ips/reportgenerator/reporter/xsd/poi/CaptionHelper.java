package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.poi;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.STFldCharTypeImpl;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class CaptionHelper {
    final CursorHelper cursorHelper;
    final BookmarkRegistry bookmarkRegistry;

    public void createTableCaption(final String stringBeforeConceptWithTrailingSpace,
                                   final BookmarkHelper bookmarkHelper,
                                   final String stringAfterConceptWithLeadingSpace) {
        createTableCaption(cursorHelper, bookmarkRegistry,
                stringBeforeConceptWithTrailingSpace, bookmarkHelper, stringAfterConceptWithLeadingSpace);
    }

    public void createFigureCaption(final String stringBeforeConceptWithTrailingSpace,
                                    final BookmarkHelper bookmarkHelper,
                                    final String stringAfterConceptWithLeadingSpace) {
        createFigureCaption(cursorHelper, bookmarkRegistry,
                stringBeforeConceptWithTrailingSpace, bookmarkHelper, stringAfterConceptWithLeadingSpace);
    }

    public static void createTableCaption(final CursorHelper cursorHelper, final BookmarkRegistry bookmarkRegistry,
                                          final String stringBeforeConceptWithTrailingSpace,
                                          final BookmarkHelper bookmarkHelper,
                                          final String stringAfterConceptWithLeadingSpace) {
        createFloatCaption(cursorHelper, bookmarkRegistry,
                stringBeforeConceptWithTrailingSpace, bookmarkHelper, stringAfterConceptWithLeadingSpace,
                "Table ", "Tabelle", VdvStyle.TABELLEBERSCHRIFT);
    }

    public static void createFigureCaption(final CursorHelper cursorHelper, final BookmarkRegistry bookmarkRegistry,
                                           final String stringBeforeConceptWithTrailingSpace,
                                           final BookmarkHelper bookmarkHelper,
                                           final String stringAfterConceptWithLeadingSpace) {
        createFloatCaption(cursorHelper, bookmarkRegistry,
                stringBeforeConceptWithTrailingSpace, bookmarkHelper, stringAfterConceptWithLeadingSpace,
                "Figure ", "Abbildung", VdvStyle.GRAFIKTITEL);
    }

    public static void createFloatCaption(final CursorHelper cursorHelper,
                                          final BookmarkRegistry bookmarkRegistry,
                                          final String stringBeforeConceptWithTrailingSpace,
                                          final BookmarkHelper bookmarkHelper,
                                          final String stringAfterConceptWithLeadingSpace,
                                          final String floatTypeNameWithTrailingSpace,
                                          final String sequenceName,
                                          final VdvStyle style) {
        final XWPFParagraph paragraph = cursorHelper.createParagraph();
        style.applyTo(paragraph);
        try (final BookmarkRegistry.Helper ignored = bookmarkRegistry.createBookmark(paragraph, bookmarkHelper
                .toFloatLabel())) {
            paragraph.createRun().setText(floatTypeNameWithTrailingSpace);
            paragraph.createRun().getCTR().addNewFldChar().setFldCharType(STFldCharTypeImpl.BEGIN);
            {
                final CTText instrText = paragraph.createRun().getCTR().addNewInstrText();
                instrText.setSpace(SpaceAttribute.Space.PRESERVE);
                instrText.setStringValue("SEQ " + sequenceName + " \\* ARABIC ");
            }
            paragraph.createRun().getCTR().addNewFldChar().setFldCharType(STFldCharTypeImpl.SEPARATE);
            paragraph.createRun().setText("refresh-me");
            paragraph.createRun().getCTR().addNewFldChar().setFldCharType(STFldCharTypeImpl.END);
        }
        paragraph.createRun().setText(": " + stringBeforeConceptWithTrailingSpace);
        try (final BookmarkRegistry.Helper ignored = bookmarkRegistry.createBookmark(paragraph, bookmarkHelper
                .toConceptLabel())) {
            paragraph.createRun().setText(bookmarkHelper.getOriginalName());
        }
        paragraph.createRun().setText(stringAfterConceptWithLeadingSpace);
    }
}
