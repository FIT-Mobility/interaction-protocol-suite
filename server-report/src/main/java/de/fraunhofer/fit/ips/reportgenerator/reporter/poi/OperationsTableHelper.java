package de.fraunhofer.fit.ips.reportgenerator.reporter.poi;

import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

import javax.xml.namespace.QName;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class OperationsTableHelper extends TableHelper {

    public OperationsTableHelper(final Context context,
                                 final CursorHelper cursorHelper) {
        super(context, cursorHelper.createTable());
        setUpXmlTableStructure();
        setHeaderRow();
    }

    protected void setMinimalSpacing(final CTPPr pProps) {
        // don't set special spacing
    }

    private void setUpXmlTableStructure() {
        table.setStyleID(VdvStyle.NORMAL.getStyleId());

        final CTTbl tableCore = table.getCTTbl();
        {
            final CTTblPr tableProps = tableCore.getTblPr();
            tableProps.addNewTblLayout().setType(STTblLayoutType.FIXED);
        }

        final CTTblGrid tableGrid = tableCore.addNewTblGrid();
        addColumnToGrid(tableGrid, 5.53);
        addColumnToGrid(tableGrid, 2);
        addColumnToGrid(tableGrid, 7.13);
    }

    private void setHeaderRow() {
        assert 1 == table.getNumberOfRows() : "setHeaderRow should only be called once before everything else!";
        final XWPFTableRow row = table.getNumberOfRows() == 0 ? table.createRow() : table.getRow(0);
        row.setRepeatHeader(true);
        createCellHelper(row, 0, Format.BOLD).text("Operation");
        createCellHelper(row, 1, Format.BOLD).text("Request / Response");
        createCellHelper(row, 2, Format.BOLD).text("Used data type, Data structure");
    }

    public void addOperation(final String operationName, final QName requestDT, final QName responseDT) {
        createRow(operationName, "Req.", requestDT)
                .getCell(0).getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
        createRow("", "Resp.", responseDT)
                .getCell(0).getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
    }

    private XWPFTableRow createRow(final String operation, final String rowID, final QName dataType) {
        final XWPFTableRow row = table.createRow();
        createCellHelper(row, 0, Format.NORMAL).text(operation);
        createCellHelper(row, 1, Format.NORMAL).text(rowID);
        smartPrettyPrint(createCellHelper(row, 2, Format.NORMAL), Format.NORMAL, dataType, false, false);
        return row;
    }
}
