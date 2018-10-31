package de.fraunhofer.fit.omp.reportgenerator.playground;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.XmlCursor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class XmlCursorPlayground {
    public static void main(String[] args) throws IOException {
        final String inputfile = "src/test/resources/playground-template.docx";
        try (final XWPFDocument document = new XWPFDocument(new FileInputStream(inputfile))) {
            final XmlCursor xmlCursor = findMagicToken(document).getCTP().newCursor();
            try {
                xmlCursor.toNextSibling();

                final XWPFParagraph p1 = document.insertNewParagraph(xmlCursor);
                xmlCursor.toParent();
                xmlCursor.toNextSibling();

                p1.createRun().setText("La La Land");


                final XWPFParagraph p2 = document.insertNewParagraph(xmlCursor);
                p2.createRun().setText("Bullshit Bingo");

                xmlCursor.toParent();
                xmlCursor.toNextSibling();

                final List<XWPFParagraph> paragraphs = document.getParagraphs();
                for (XWPFParagraph paragraph : paragraphs) {
                    log.warn(paragraph.getText());
                }
            } finally {
                xmlCursor.dispose();
            }
        }
    }

    private static XWPFParagraph findMagicToken(final XWPFDocument document) {
        final String searchString = "Dienst PassengerCountingService";
        final ImmutableList<XWPFParagraph> paragraphs = ImmutableList.copyOf(document.getParagraphs());
        final PositionInParagraph startPos = new PositionInParagraph();
        final int paragraphsSize = paragraphs.size();
        int i = 0;
        while (i < paragraphsSize) {
            if (null != paragraphs.get(i).searchText(searchString, startPos)) {
                break;
            }
            ++i;
        }
        return paragraphs.get(i);
    }
}