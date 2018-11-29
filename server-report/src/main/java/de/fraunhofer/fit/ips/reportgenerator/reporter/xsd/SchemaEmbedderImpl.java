package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd;

import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.poi.BookmarkRegistry;
import de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.poi.PrefixHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 * @since 23.01.2018
 */
@Slf4j
public class SchemaEmbedderImpl implements SchemaEmbedder {

    private static final String DIMO_PREFIX = "{{#dimo#";
    private static final String DIMO_SUFFIX = "}}";
    private static final String COMMON_DATA_TYPES_STRING = "common-data-types";
    private static final String SEP = "#";
    private static final String FUNCTION = "function";

    @Override
    public byte[] process(final Schema schema, final byte[] docx) {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final BufferedOutputStream stream = new BufferedOutputStream(bos);
             final XWPFDocument xwpfDocument = processAndReturnPOI(schema, docx)) {
            xwpfDocument.write(stream);
            return bos.toByteArray();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public XWPFDocument processAndReturnPOI(final Schema schema, final byte[] docx) {
        final XWPFDocument document = createDocument(docx);

        final PrefixHelper prefixHelper = new PrefixHelper(document.getPackagePart());
        final BookmarkRegistry bookmarkRegistry = new BookmarkRegistry();
//        final Context context = new Context(reportConfiguration, prefixHelper, bookmarkRegistry, document, schema);
        final PositionInParagraph pseudoStartPos = new PositionInParagraph();

//        ImmutableList.copyOf(document.getParagraphs())
//                     .stream()
//                     .filter(par -> Objects.nonNull(par.searchText(DIMO_PREFIX, pseudoStartPos)))
//                     .forEach(par -> doYourMagic(context, par));

        document.enforceUpdateFields();
        return document;
    }

    private XWPFDocument createDocument(byte[] docx) {
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(docx)) {
            return new XWPFDocument(bis);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

//
//    private void doYourMagic(final Context context, final XWPFParagraph paragraph) {
//        try (final CursorHelper cursorHelper = CursorHelper.fromParagraph(context.getDocument(), paragraph)) {
//            final String paragraphText = paragraph.getText();
//            final String innerString = paragraphText.substring(DIMO_PREFIX.length(), paragraphText.length() - DIMO_SUFFIX.length());
//            // paragraphText ~= {{#dimo#common-data-types}}
//            if (COMMON_DATA_TYPES_STRING.equals(innerString)) {
//                insertCommonDataTypes(context, cursorHelper);
//                return;
//            }
//            // paragraphText ~= {{#dimo#function#FUNCTIONNAME}}
//            final String[] split = innerString.split(SEP);
//            if (split.length == 2 && split[0].equals(FUNCTION)) {
//                String operationName = split[1];
//                Function operation = context.getSchemaOperations().get(operationName);
//                if (null == operation) {
//                    log.error("function {} is not present in map of functions!", operationName);
//                } else {
//                    VdvTables.handleFunctionWithoutHeading(context, cursorHelper, operation);
//                }
//                return;
//            }
//            log.error("found {} in '{}', but could not understand its meaning!", DIMO_PREFIX, paragraphText);
//        } finally {
//            context.getDocument().removeBodyElement(context.getDocument().getPosOfParagraph(paragraph));
//        }
//    }
//
//    private void insertCommonDataTypes(Context context, CursorHelper cursorHelper) {
//        final CaptionHelper captionHelper = new CaptionHelper(cursorHelper, context.getBookmarkRegistry());
//        final VdvTables.SimpleTypeTableBookmarker common = VdvTables.SimpleTypeTableBookmarker.common();
//
//        try (final VdvTables.Processor processor = new VdvTables.Processor(context, cursorHelper, captionHelper, common)) {
//            for (final QName commonConceptName : context.getDependencyHelper().commonConceptNames) {
//                VdvTables.processCorrectly(context, processor, commonConceptName);
//            }
//        }
//    }
}
