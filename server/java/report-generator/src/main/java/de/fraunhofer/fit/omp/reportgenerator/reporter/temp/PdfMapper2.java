package de.fraunhofer.fit.omp.reportgenerator.reporter.temp;

import com.lowagie.text.Chunk;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.poi.xwpf.converter.pdf.internal.PdfMapper;
import fr.opensagres.xdocreport.itext.extension.IITextContainer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;

import java.io.OutputStream;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 20.02.2018
 */
public class PdfMapper2 extends PdfMapper {

    public PdfMapper2(XWPFDocument document, OutputStream out,
                      PdfOptions options, Integer expectedPageCount) throws Exception {
        super(document, out, options, expectedPageCount);
    }

    /**
     * https://github.com/FIT-Mobility/xdocreport/commit/821785441f97b92171eadb5e42afe6cc6f2b207e
     */
    @Override
    protected void visitBookmark(CTBookmark bookmark, XWPFParagraph paragraph,
                                 IITextContainer paragraphContainer) throws Exception {
        // destination for a local anchor
        // chunk with empty text does not work as local anchor
        // so we create chunk with invisible but not empty text content
        // if bookmark is the last chunk in a paragraph something must be added
        // after or it does not work
        Chunk chunk = new Chunk("\u2063"); // Unicode Invisible Separator (U+2063)
        chunk.setLocalDestination(bookmark.getName());
        paragraphContainer.addElement(chunk);
    }
}
