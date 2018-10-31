package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class EnumerationTableHelper extends TableHelper {

    public EnumerationTableHelper(final Context context,
                                  final CursorHelper cursorHelper,
                                  final String typeName,
                                  final QName baseType,
                                  final List<String> description) {
        super(context, cursorHelper.createTable());
        setUpXmlTableStructure();
        setHeaderRow(typeName, baseType, description);
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
        addColumnToGrid(tableGrid, 5.94);
        // Beschreibung
        addColumnToGrid(tableGrid, 9.48);
    }

    private void setHeaderRow(final String typeName, final QName baseType, final List<String> description) {
        assert 1 == table.getNumberOfRows() : "setHeaderRow should only be called once before everything else!";
        final XWPFTableRow row = table.getNumberOfRows() == 0 ? table.createRow() : table.getRow(0);
        row.setRepeatHeader(true);
        // createCellHelper(row, 0, Format.BOLD_ITALICS).text(typeName);
        {
            final ParagraphHelper.RunHelper cellHelper = createCellHelper(row, 0, Format.BOLD_ITALICS);
            cellHelper.text("restriction of ");
            smartPrettyPrint(cellHelper, Format.BOLD_ITALICS, baseType, true, false);

        }
        insertDocumentation(createCellHelper(row, 1, Format.BOLD), description);
    }

    public void addValue(final String valueName, final List<String> description) {
        final XWPFTableRow row = table.createRow();
        createCellHelper(row, 0, Format.NORMAL).text(valueName);
        insertDocumentation(createCellHelper(row, 1, Format.NORMAL), description);
    }
}
