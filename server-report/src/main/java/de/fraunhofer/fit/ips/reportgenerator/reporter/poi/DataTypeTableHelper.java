package de.fraunhofer.fit.ips.reportgenerator.reporter.poi;

import de.fraunhofer.fit.ips.model.xsd.Attributes;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class DataTypeTableHelper extends TableHelper {

    private static final QName WILDCARD_ATTRIBUTE_NAME = new QName(SchemaSymbols.URI_SCHEMAFORSCHEMA, "#" + SchemaSymbols.ELT_ANYATTRIBUTE);
    private static final QName WILDCARD_ATTRIBUTE_TYPE = new QName(SchemaSymbols.URI_SCHEMAFORSCHEMA, SchemaSymbols.ELT_ANYATTRIBUTE);

    public DataTypeTableHelper(final Context context,
                               final CursorHelper cursorHelper,
                               final QName typeName,
                               final List<String> description) {
        super(context, cursorHelper.createTable());
        setUpXmlTableStructure();
        setHeaderRow(typeName, description);
    }

    private void setUpXmlTableStructure() {
        // table width 15.42 cm
        // column 1 width 1.46 cm
        // column 2 width 0.42 cm
        // column 3 width 3.08 cm
        // column 4 width 1.27 cm
        // column 5 width 2.22 cm
        // column 6 width 6.98 cm
        table.setStyleID(VdvStyle.NORMAL.getStyleId());
        final CTTbl tableCore = table.getCTTbl();
        {
            final CTTblPr tableProps = tableCore.getTblPr();
            tableProps.addNewTblLayout().setType(STTblLayoutType.FIXED);
            tableProps.addNewTblW().setW(cmToTwipsAsBI(15.42));
        }

        final CTTblGrid tableGrid = tableCore.addNewTblGrid();
        // Gruppierung
        addColumnToGrid(tableGrid, 1.46);
        // alphanumerische Durchnummerierung bei Choices
        addColumnToGrid(tableGrid, 0.42);
        // Elementname
        addColumnToGrid(tableGrid, 3.08);
        // Min : Max
        addColumnToGrid(tableGrid, 1.27);
        // Datentyp
        addColumnToGrid(tableGrid, 2.22);
        // Erläuterung
        addColumnToGrid(tableGrid, 6.98);
    }

    private void setHeaderRow(final QName typeName, final List<String> description) {
        assert 1 == table.getNumberOfRows() : "setHeaderRow should only be called once before everything else!";
        final XWPFTableRow row = table.getNumberOfRows() == 0 ? table.createRow() : table.getRow(0);
        // row.setRepeatHeader(true);
        smartPrettyPrint(createCellHelper(row, 0, Format.BOLD_ITALICS), Format.BOLD_ITALICS, typeName, false, false);
//        createCellHelper(row, 0, Format.BOLD_ITALICS).text(Constants.improveCamelCaseLineBreaks(typeName));
        setGridSpan(row, 0, 4);
        createCellHelper(row, 1, Format.ITALICS).text("+Structure");
        insertDocumentation(createCellHelper(row, 2, Format.BOLD), description);
    }

    public void addExtensionRow(final QName baseType) {
        addExtensionRow(baseType, (row) -> createCellHelper(row, 0, Format.ITALICS).text(""));
    }

    public void addRestrictionRow(final QName baseType) {
        addRestrictionRow(baseType, (row) -> createCellHelper(row, 0, Format.ITALICS).text(""));
    }

    public void addExtensionRow(final QName baseType, final Consumer<XWPFTableRow> createFirstColumn) {
        createExtensionRestrictionRow(baseType, ":::", "extension", createFirstColumn);
    }

    public void addRestrictionRow(final QName baseType, final Consumer<XWPFTableRow> createFirstColumn) {
        createExtensionRestrictionRow(baseType, "!!!", "restriction", createFirstColumn);
    }

    private void createExtensionRestrictionRow(final QName baseType, final String derivedBySymbol,
                                               final String derivedByString,
                                               final Consumer<XWPFTableRow> createFirstColumn) {
        final XWPFTableRow row = table.createRow();
        createFirstColumn.accept(row);
        createCellHelper(row, 1, Format.BOLD_ITALICS).text(derivedBySymbol);
        setGridSpan(row, 1, 2);
        createCellHelper(row, 2, Format.BOLD).text("1:1");
        smartPrettyPrint(createCellHelper(row, 3, Format.ITALICS), Format.ITALICS, baseType, true, false);
        {
            final ParagraphHelper.RunHelper runHelper = createCellHelper(row, 4, Format.NORMAL)
                    .text("derived by " + derivedByString);
            if (context.getSchema().getInternalConceptNames().contains(baseType)) {
                runHelper
                        .text(", see ")
                        .bookmarkRef(new BookmarkHelper(baseType.getLocalPart()), BookmarkHelper::toFloatLabel);
            }
        }
    }

    private static boolean isMandatory(final String cardinality) {
        return !cardinality.split(":")[0].equals("0");
    }

    public static DataTypeTableHelper.Format reformat(final String cardinality,
                                                      final DataTypeTableHelper.Format toReformat) {
        return isMandatory(cardinality) ? Format.BOLD.combine(toReformat) : toReformat;
    }

    @RequiredArgsConstructor
    public class GroupHelper implements AutoCloseable {
        final int startRow = DataTypeTableHelper.this.table.getNumberOfRows();
        final QName groupName;

        public void addRestrictionRow(final QName baseType) {
            DataTypeTableHelper.this.addRestrictionRow(baseType, (row) -> smartPrettyPrint(createCellHelper(row, 0, Format.ITALICS), Format.ITALICS, groupName, false, false));
        }

        public void addElement(final String cardinality,
                               final QName elementName,
                               final QName dataType,
                               final List<String> description) {
            final XWPFTableRow row = table.createRow();
            smartPrettyPrint(createCellHelper(row, 0, Format.ITALICS), Format.ITALICS, groupName, false, false);
            smartPrettyPrint(createCellHelper(row, 1, reformat(cardinality, Format.ITALICS)), reformat(cardinality, Format.ITALICS), elementName, false, false);
            setGridSpan(row, 1, 2);
            createCellHelper(row, 2, reformat(cardinality, Format.NORMAL)).text(cardinality);
            smartPrettyPrint(createCellHelper(row, 3, Format.ITALICS), Format.ITALICS, dataType, true, true);
            insertDocumentation(createCellHelper(row, 4, Format.NORMAL), description);
        }

        public void addAttribute(final boolean required,
                                 final QName attributeName,
                                 final QName dataType,
                                 final Attributes.AttributeDefaultOrFixedValue defaultOrFixedValue,
                                 final List<String> description) {
            final String cardinality = required ? "1:1" : "0:1";
            final XWPFTableRow row = table.createRow();
            smartPrettyPrint(createCellHelper(row, 0, Format.ITALICS), Format.ITALICS, groupName, false, false);
            final QName hack = null != context.getConcept(attributeName) ? attributeName : new QName(attributeName.getNamespaceURI(), "#" + attributeName.getLocalPart(), attributeName.getPrefix());
            smartPrettyPrint(createCellHelper(row, 1, reformat(cardinality, Format.ITALICS)), reformat(cardinality, Format.ITALICS), hack, true, false);
            setGridSpan(row, 1, 2);
            createCellHelper(row, 2, reformat(cardinality, Format.NORMAL)).text(cardinality);
            smartPrettyPrint(createCellHelper(row, 3, Format.ITALICS), Format.ITALICS, dataType, true, true);
            insertDocumentation(createCellHelper(row, 4, Format.NORMAL), prependDefaultOrFixed(defaultOrFixedValue, description));
        }

        public void addWildcard(final Attributes.AnyAttribute anyAttribute) {
            final XWPFTableRow row = table.createRow();
            smartPrettyPrint(createCellHelper(row, 0, Format.ITALICS), Format.ITALICS, groupName, false, false);
            smartPrettyPrint(createCellHelper(row, 1, Format.BOLD_ITALICS), Format.BOLD_ITALICS, WILDCARD_ATTRIBUTE_NAME, false, false);
            setGridSpan(row, 1, 2);
            createCellHelper(row, 2, Format.BOLD).text("1:1");
            smartPrettyPrint(createCellHelper(row, 3, Format.ITALICS), Format.ITALICS, WILDCARD_ATTRIBUTE_TYPE, false, false);
            insertDocumentation(createCellHelper(row, 4, Format.NORMAL), Constants.getDocs(context, anyAttribute.getDocs()));
        }

        @Override
        public void close() {
            final int endRow = table.getNumberOfRows() - 1;
            mergeCellsVertically(0, startRow, endRow);
        }

        public GroupHelper.ChoiceHelper startChoice(final String cardinality) {
            return new ChoiceHelper(cardinality);
        }

        @RequiredArgsConstructor
        public class ChoiceHelper implements AutoCloseable {
            final String cardinality;
            final int startRow = DataTypeTableHelper.this.table.getNumberOfRows();
            char counter = 'a';

            public void addOption(final QName elementName,
                                  final QName dataType,
                                  final List<String> description) {
                final XWPFTableRow row = table.createRow();
                smartPrettyPrint(createCellHelper(row, 0, Format.ITALICS), Format.ITALICS, groupName, false, false);
                createCellHelper(row, 1, Format.ITALICS).text(String.valueOf(counter));
                smartPrettyPrint(createCellHelper(row, 2, Format.BOLD_ITALICS), Format.BOLD_ITALICS, elementName, false, false);
                smartPrettyPrint(createCellHelper(row, 4, Format.ITALICS), Format.ITALICS, dataType, true, true);
                insertDocumentation(createCellHelper(row, 5, Format.NORMAL), description);
                if (counter == 'a') {
                    final String text = "-" + cardinality;
                    final Format format = isMandatory(cardinality) ? Format.BOLD : Format.NORMAL;
                    createCellHelper(row, 3, format).text(text);
                    row.getCell(3).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                }
                ++counter;
            }

            @Override
            public void close() {
                final int endRow = table.getNumberOfRows() - 1;
                mergeCellsVertically(3, startRow, endRow);
            }
        }
    }

    public GroupHelper startGroup(final QName groupName) {
        return new GroupHelper(groupName);
    }
}
