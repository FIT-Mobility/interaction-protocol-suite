package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class PrefixHelper {
    private final PackagePart packagePart;
    private final Map<String, String> cache = new HashMap<>();

    private String getRelationId(final String uri) {
        return cache.computeIfAbsent(uri, x -> packagePart.addExternalRelationship(x, XWPFRelation.HYPERLINK.getRelation()).getId());
    }

    public XWPFParagraph addHyperlink(final XWPFParagraph paragraph, final String uri, final String text,
                                      final TableHelper.Format format) {
        final CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
        cLink.setId(getRelationId(uri));
        final CTR run = cLink.addNewR();
        format.applyTo(run);
        run.addNewT().setStringValue(text);
        return paragraph;
    }
}
