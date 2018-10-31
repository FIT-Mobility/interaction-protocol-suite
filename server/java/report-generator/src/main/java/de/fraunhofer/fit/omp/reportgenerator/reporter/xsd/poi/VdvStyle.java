package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

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
}
