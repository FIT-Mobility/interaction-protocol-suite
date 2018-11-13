package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.impl.xb.xmlschema.SpaceAttribute;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSym;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShortHexNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.STFldCharTypeImpl;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class ParagraphHelper {
    final CursorHelper cursorHelper;

    public void createHeading(final VdvStyle headingStyle, final String headingText) {
        headingStyle.applyTo(cursorHelper.createParagraph()).createRun().setText(headingText);
    }

    public void createEnHeading(final VdvStyle headingStyle, final String headingText) {
        final XWPFRun run = headingStyle.applyTo(cursorHelper.createParagraph()).createRun();
        run.setText(String.valueOf(Constants.ZERO_WIDTH_SPACE));
        run.addTab();
        run.setText(headingText);
    }

    @RequiredArgsConstructor
    public static class RunHelper {
        private static final byte[] WINGDINGS_ARROW_CHAR;

        static {
            STShortHexNumber hexNumber = STShortHexNumber.Factory.newInstance();
            hexNumber.setStringValue("F0E0");
            WINGDINGS_ARROW_CHAR = hexNumber.getByteArrayValue();
        }

        final XWPFParagraph paragraph;
        final UnaryOperator<XWPFRun> runStyler;

        public RunHelper text(final String text) {
            final XWPFRun run = paragraph.createRun();
            run.setText(text);
            runStyler.apply(run);
            return this;
        }

        public RunHelper code(final String text) {
            return code(text, VdvStyle.DIENSTEZCHN);
        }

        public RunHelper code(final String text, final VdvStyle style) {
            final XWPFRun run = paragraph.createRun();
            run.setText(text);
            style.applyTo(runStyler.apply(run));
            return this;
        }

        public RunHelper lineBreak() {
            final XWPFRun run = paragraph.createRun();
            run.addBreak();
            runStyler.apply(run);
            return this;
        }

        public RunHelper pageBreak() {
            final XWPFRun run = paragraph.createRun();
            run.addBreak(BreakType.PAGE);
            runStyler.apply(run);
            return this;
        }

        public RunHelper bookmarkRef(final BookmarkHelper bookmarkHelper,
                                     final Function<BookmarkHelper, String> target) {
            runStyler.apply(paragraph.createRun()).getCTR().addNewFldChar().setFldCharType(STFldCharTypeImpl.BEGIN);
            {
                final CTText instrText = runStyler.apply(paragraph.createRun()).getCTR().addNewInstrText();
                instrText.setSpace(SpaceAttribute.Space.PRESERVE);
                instrText.setStringValue("REF " + target.apply(bookmarkHelper) + " \\h \\* MERGEFORMAT ");
            }
            runStyler.apply(paragraph.createRun()).getCTR().addNewFldChar().setFldCharType(STFldCharTypeImpl.SEPARATE);
            {
                final XWPFRun run = paragraph.createRun();
                run.setText(bookmarkHelper.getOriginalName());
                runStyler.apply(run);
            }
            runStyler.apply(paragraph.createRun()).getCTR().addNewFldChar().setFldCharType(STFldCharTypeImpl.END);
            // add empty run to prevent the text style from becoming overridden when refreshing the hyperlink
            runStyler.apply(paragraph.createRun());
            return this;
        }

        public RunHelper wingdingsArrow() {
            final XWPFRun run = paragraph.createRun();
            final CTR ctr = run.getCTR();
            final CTSym ctSym = ctr.addNewSym();
            ctSym.setFont("Wingdings");
            ctSym.setChar(WINGDINGS_ARROW_CHAR);
            runStyler.apply(run);
            return this;
        }
    }

    public RunHelper createRunHelper(final VdvStyle baseStyle) {
        return createRunHelper(baseStyle, UnaryOperator.identity());
    }

    public RunHelper createRunHelper(final VdvStyle baseStyle,
                                     final UnaryOperator<XWPFRun> runStyler) {
        return new RunHelper(
                baseStyle.applyTo(
                        cursorHelper.createParagraph()
                ),
                runStyler
        );
    }

    @RequiredArgsConstructor
    public static class ListHelper {
        final BigInteger level;
        final Function<BigInteger, XWPFParagraph> paragraphSupplier;

        public RunHelper createRunHelper() {
            return createRunHelper(UnaryOperator.identity());
        }

        public RunHelper createRunHelper(final UnaryOperator<XWPFRun> runStyler) {
            return new RunHelper(paragraphSupplier.apply(level), runStyler);
        }

        public ListHelper increaseIndent() {
            return new ListHelper(level.add(BigInteger.ONE), paragraphSupplier);
        }
    }

    public ListHelper createBulletListHelper() {
        return new ListHelper(BigInteger.ZERO, (level) -> styleList(cursorHelper, level, Constants.BULLET_LIST));
    }

    public ListHelper createNumberedListHelper() {
        return new ListHelper(BigInteger.ZERO, (level) -> styleList(cursorHelper, level, Constants.NUMBERED_LIST));
    }

    public ListHelper createSilentListHelper() {
        return new ListHelper(BigInteger.ZERO, (level) -> fixIndent(styleList(cursorHelper, level, Constants.SILENT_LIST)));
    }

    private static XWPFParagraph styleList(final CursorHelper cursorHelper, final BigInteger ilvl,
                                           final BigInteger numId) {
        final XWPFParagraph paragraph = VdvStyle.LISTPARAGRAPH.applyTo(cursorHelper.createParagraph());
        final CTP ctp = paragraph.getCTP();
        final CTPPr pProps = Optional.ofNullable(ctp.getPPr())
                                     .orElseGet(ctp::addNewPPr);
        final CTSpacing spacing = Optional.ofNullable(pProps.getSpacing())
                                          .orElseGet(pProps::addNewSpacing);
        spacing.setAfter(BigInteger.ZERO);
        spacing.setLine(BigInteger.valueOf(240));
        spacing.setLineRule(STLineSpacingRule.AUTO);
        pProps.addNewContextualSpacing();
        final CTNumPr numProps = Optional.ofNullable(pProps.getNumPr())
                                         .orElseGet(pProps::addNewNumPr);
        Optional.ofNullable(numProps.getIlvl())
                .orElseGet(numProps::addNewIlvl)
                .setVal(ilvl);
        Optional.ofNullable(numProps.getNumId())
                .orElseGet(numProps::addNewNumId)
                .setVal(numId);
        return paragraph;
    }

    private static XWPFParagraph fixIndent(final XWPFParagraph paragraph) {
        final CTP ctp = paragraph.getCTP();
        final CTPPr pProps = Optional.ofNullable(ctp.getPPr())
                                     .orElseGet(ctp::addNewPPr);
        final CTInd ind = Optional.ofNullable(pProps.getInd())
                                  .orElseGet(pProps::addNewInd);
        ind.setLeft(BigInteger.valueOf(720));
        return paragraph;
    }
}
