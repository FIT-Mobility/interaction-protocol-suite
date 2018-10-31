package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlCursor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
public class CursorHelper implements AutoCloseable {
    final XWPFDocument document;
    final XmlCursor cursor;

    public static CursorHelper fromParagraph(final XWPFDocument document, final XWPFParagraph paragraph) {
        final XmlCursor xmlCursor = paragraph.getCTP().newCursor();
        xmlCursor.toNextSibling();
        return new CursorHelper(document, xmlCursor);
    }

    public XWPFParagraph createParagraph() {
        final XWPFParagraph paragraph = document.insertNewParagraph(cursor);
        cursor.toParent();
        cursor.toNextSibling();
        return paragraph;
    }

    public XWPFTable createTable() {
        final XWPFTable table = document.insertNewTbl(cursor);
        cursor.toParent();
        cursor.toNextSibling();
        return table;
    }

    @Override
    public void close() {
        cursor.dispose();
    }
}
