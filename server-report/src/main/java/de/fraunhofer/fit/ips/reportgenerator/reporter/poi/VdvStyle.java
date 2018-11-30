package de.fraunhofer.fit.ips.reportgenerator.reporter.poi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;

import java.util.Optional;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public enum VdvStyle {
    HEADING_1("Heading1"),
    HEADING_2("Heading2"),
    HEADING_3("Heading3"),
    HEADING_4("Heading4"),
    HEADING_5("Heading5"),
    HEADING_6("Heading6"),
    HEADING_7("Heading7"),
    HEADING_8("Heading8"),
    HEADING_9("Heading9"),
    BERSCHRIFT_ENGLISCH("berschriftEnglisch"),
    TABELLEBERSCHRIFT("Tabelleberschrift"),
    GRAFIKTITEL("Grafiktitel"),
    CAPTION("Caption"),
    DIENSTEZCHN("DiensteZchn"),
    LISTPARAGRAPH("ListParagraph"),
    NORMAL("Normal");

    @Getter final String styleId;

    public XWPFParagraph applyTo(final XWPFParagraph paragraph) {
        paragraph.setStyle(styleId);
        return paragraph;
    }

    public XWPFRun applyTo(final XWPFRun run) {
        final CTR runCore = run.getCTR();
        final CTRPr rProps = Optional.ofNullable(runCore.getRPr()).orElseGet(runCore::addNewRPr);
        final CTString rStyle = Optional.ofNullable(rProps.getRStyle()).orElseGet(rProps::addNewRStyle);
        rStyle.setVal(styleId);
        return run;
    }

    public static VdvStyle getHeadingLevel(final int oneBasedLevel, final boolean primary) {
        if (!primary) {
            return BERSCHRIFT_ENGLISCH;
        }
        switch (oneBasedLevel) {
            case 1:
                return HEADING_1;
            case 2:
                return HEADING_2;
            case 3:
                return HEADING_3;
            case 4:
                return HEADING_4;
            case 5:
                return HEADING_5;
            case 6:
                return HEADING_6;
            case 7:
                return HEADING_7;
            case 8:
                return HEADING_8;
            case 9:
                return HEADING_9;
            default:
                // TODO warn?
                return NORMAL;
        }
    }
}
