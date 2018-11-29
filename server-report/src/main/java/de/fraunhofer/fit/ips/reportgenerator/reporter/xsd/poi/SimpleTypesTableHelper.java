package de.fraunhofer.fit.ips.reportgenerator.reporter.xsd.poi;

import de.fraunhofer.fit.ips.reportgenerator.model.xsd.Type;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class SimpleTypesTableHelper extends TableHelper {

    public SimpleTypesTableHelper(final Context context,
                                  final CursorHelper cursorHelper) {
        super(context, cursorHelper.createTable());
        setUpXmlTableStructure();
        setHeaderRow();
    }

    private void setUpXmlTableStructure() {
        // table width 15.42 cm

        // three columns
        // column 1 width 3.44 cm
        // column 2 width 5cm
        // column 3 width 6.98 cm

        table.setStyleID(VdvStyle.NORMAL.getStyleId());
        final CTTbl tableCore = table.getCTTbl();
        {
            final CTTblPr tableProps = tableCore.getTblPr();
            tableProps.addNewTblLayout().setType(STTblLayoutType.FIXED);
            tableProps.addNewTblW().setW(cmToTwipsAsBI(15.42));
        }

        final CTTblGrid tableGrid = tableCore.addNewTblGrid();
        // Typname
        addColumnToGrid(tableGrid, 3.44);
        // Werte
        addColumnToGrid(tableGrid, 5);
        // Beschreibung
        addColumnToGrid(tableGrid, 6.98);
    }

    private void setHeaderRow() {
        assert 1 == table.getNumberOfRows() : "setHeaderRow should only be called once before everything else!";
        final XWPFTableRow row = table.getNumberOfRows() == 0 ? table.createRow() : table.getRow(0);
        row.setRepeatHeader(true);
        createCellHelper(row, 0, Format.BOLD_ITALICS).text("type name");
        createCellHelper(row, 1, Format.BOLD_ITALICS).text("base type");
        createCellHelper(row, 2, Format.BOLD).text("description");
    }

    public void addRestriction(final Type.Simple.Restriction restriction) {
        final XWPFTableRow row = table.createRow();
        {
            final ParagraphHelper.RunHelper cellHelper = createCellHelper(row, 0, Format.ITALICS);
            final BookmarkHelper bookmarkHelper = new BookmarkHelper(restriction.getName().getLocalPart());
            try (final BookmarkRegistry.Helper ignored = context.bookmarkRegistry.createBookmark(cellHelper.paragraph, bookmarkHelper.toConceptLabel())) {
                cellHelper.text(bookmarkHelper.getOriginalName());
            }
        }
        {
            final ParagraphHelper.RunHelper cellHelper =
                    smartPrettyPrint(createCellHelper(row, 1, Format.ITALICS), Format.ITALICS, restriction.getBaseType(), true, false);
            final String minMaxRange = restriction.getMinMaxRange();
            if (minMaxRange != null) {
                cellHelper.text(", " + minMaxRange);
            }
        }
        insertDocumentation(createCellHelper(row, 2, Format.NORMAL), Constants.getDocs(restriction.getDocs()));
    }

    public void addList(final Type.Simple.List list) {
        final XWPFTableRow row = table.createRow();
        {
            final ParagraphHelper.RunHelper cellHelper = createCellHelper(row, 0, Format.ITALICS);
            final BookmarkHelper bookmarkHelper = new BookmarkHelper(list.getName().getLocalPart());
            try (final BookmarkRegistry.Helper ignored = context.bookmarkRegistry.createBookmark(cellHelper.paragraph, bookmarkHelper.toConceptLabel())) {
                cellHelper.text(bookmarkHelper.getOriginalName());
            }
        }
        createCellHelper(row, 1, Format.ITALICS).text("list");
        insertDocumentation(createCellHelper(row, 2, Format.NORMAL), Constants.getDocs(list.getDocs()));
    }

    public void addUnion(final Type.Simple.Union union) {
        final XWPFTableRow row = table.createRow();
        {
            final ParagraphHelper.RunHelper cellHelper = createCellHelper(row, 0, Format.ITALICS);
            final BookmarkHelper bookmarkHelper = new BookmarkHelper(union.getName().getLocalPart());
            try (final BookmarkRegistry.Helper ignored = context.bookmarkRegistry.createBookmark(cellHelper.paragraph, bookmarkHelper.toConceptLabel())) {
                cellHelper.text(bookmarkHelper.getOriginalName());
            }
        }
        createCellHelper(row, 1, Format.ITALICS).text("union");
        insertDocumentation(createCellHelper(row, 2, Format.NORMAL), Constants.getDocs(union.getDocs()));
    }
}
