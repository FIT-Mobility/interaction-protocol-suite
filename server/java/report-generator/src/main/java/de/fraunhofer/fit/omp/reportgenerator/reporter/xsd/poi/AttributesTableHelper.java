package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Attributes;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class AttributesTableHelper extends TableHelper {

    public AttributesTableHelper(final Context context,
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
        // Attributname
        addColumnToGrid(tableGrid, 3.44);
        // Typ
        addColumnToGrid(tableGrid, 5);
        // Beschreibung
        addColumnToGrid(tableGrid, 6.98);
    }

    private void setHeaderRow() {
        assert 1 == table.getNumberOfRows() : "setHeaderRow should only be called once before everything else!";
        final XWPFTableRow row = table.getNumberOfRows() == 0 ? table.createRow() : table.getRow(0);
        row.setRepeatHeader(true);
        createCellHelper(row, 0, Format.BOLD_ITALICS).text("attribute name");
        createCellHelper(row, 1, Format.BOLD_ITALICS).text("type");
        createCellHelper(row, 2, Format.BOLD).text("description");
    }

    public void addAttributeDeclaration(final Attributes.GlobalAttributeDeclaration attributeDeclaration) {
        final XWPFTableRow row = table.createRow();
        {
            final ParagraphHelper.RunHelper cellHelper = createCellHelper(row, 0, Format.ITALICS);
            cellHelper.text("#");
            final BookmarkHelper bookmarkHelper = new BookmarkHelper(attributeDeclaration.getName().getLocalPart());
            try (final BookmarkRegistry.Helper ignored = context.bookmarkRegistry.createBookmark(cellHelper.paragraph, bookmarkHelper.toConceptLabel())) {
                cellHelper.text(bookmarkHelper.getOriginalName());
            }
        }
        smartPrettyPrint(createCellHelper(row, 1, Format.ITALICS), Format.ITALICS, attributeDeclaration.getType(), true, false);
        insertDocumentation(createCellHelper(row, 2, Format.NORMAL), prependDefaultOrFixed(attributeDeclaration.getDefaultOrFixedValue(), Constants.getDocs(attributeDeclaration.getDocs())));
    }
}
