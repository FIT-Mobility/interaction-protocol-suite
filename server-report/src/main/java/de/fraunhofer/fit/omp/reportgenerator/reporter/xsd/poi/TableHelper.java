package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import com.google.common.collect.Iterables;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Attributes;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Element;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.NamedConceptWithOriginVisitor;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Type;
import de.fraunhofer.fit.omp.reportgenerator.reporter.Config;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTParaRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TableHelper {
    final Context context;
    final XWPFTable table;

    protected static Iterable<String> prependDefaultOrFixed(
            final Attributes.AttributeDefaultOrFixedValue defaultOrFixedValue,
            final Iterable<String> docs) {
        if (Attributes.AttributeDefaultOrFixedValue.NONE == defaultOrFixedValue) {
            return docs;
        }
        @Nullable final String defaultValue = defaultOrFixedValue.getDefaultValue();
        if (null != defaultValue) {
            return Iterables.concat(Collections.singleton(
                    "default value: " + defaultValue
            ), docs);
        }
        @Nullable final String fixedValue = defaultOrFixedValue.getFixedValue();
        if (null != fixedValue) {
            return Iterables.concat(Collections.singleton(
                    "fixed value: " + fixedValue
            ), docs);
        }
        return docs;
    }

    private void prependSymbol(final ParagraphHelper.RunHelper run, final QName qname) {
        // prepend '+' for complex types and '-->' for group ref types
        // don't prepend anything for simple types

        // preliminarily: prepend # for attributes and --> for attribute groups
        final NamedConceptWithOrigin concept = context.getConcept(qname);
        if (null == concept) {
            return;
        }
        concept.accept(new NamedConceptWithOriginVisitor() {
            @Override
            public void visit(final Type.Complex complex) {
                run.text("+");
            }

            @Override
            public void visit(final Type.Group group) {
                run.wingdingsArrow();
            }

            @Override
            public void visit(final Attributes.GlobalAttributeGroupDeclaration globalAttributeGroupDeclaration) {
                run.wingdingsArrow();
            }

            @Override
            public void visit(final Attributes.GlobalAttributeDeclaration globalAttributeDeclaration) {
                run.text("#");
            }

            @Override
            public void visit(final Element element) {
            }

            @Override
            public void visit(final Type.Simple.Restriction restriction) {
            }

            @Override
            public void visit(final Type.Simple.Enumeration enumeration) {
            }

            @Override
            public void visit(final Type.Simple.List list) {
            }

            @Override
            public void visit(final Type.Simple.Union union) {
            }
        });
    }

    private ParagraphHelper.RunHelper createPrefix(ParagraphHelper.RunHelper run, Format format, QName qname) {
        // don't prefix non-top-level concepts
        if (null == context.getConcept(qname)) {
            return run;
        }
        final String prefix = context.schema.getPrefix(qname.getNamespaceURI());
        if ("".equals(prefix)) {
            return run;
        }
        context.prefixHelper.addHyperlink(run.paragraph, qname.getNamespaceURI(), prefix, format);
        return run.text(":" + Constants.ZERO_WIDTH_SPACE);
    }

    private ParagraphHelper.RunHelper createForwardLink(ParagraphHelper.RunHelper run, QName qname) {
        return run.bookmarkRef(new BookmarkHelper(qname.getLocalPart()), BookmarkHelper::toConceptLabel);
    }


    protected ParagraphHelper.RunHelper dumbPrettyPrint(final ParagraphHelper.RunHelper run,
                                                        final Format format,
                                                        final QName qname,
                                                        final boolean prependSymbol,
                                                        final boolean prependPrefix,
                                                        final boolean forwardLink) {
        if (prependSymbol) {
            prependSymbol(run, qname);
        }
        if (prependPrefix) {
            createPrefix(run, format, qname);
        }
        if (forwardLink) {
            createForwardLink(run, qname);
        } else {
            run.text(Constants.improveCamelCaseLineBreaks(qname.getLocalPart()));
        }
        return run;
    }

    protected ParagraphHelper.RunHelper smartPrettyPrint(final ParagraphHelper.RunHelper run,
                                                         final Format format,
                                                         final QName qname,
                                                         final boolean prependSymbol,
                                                         final boolean considerInlining) {
        // integrate qname into run:
        // QName can contain a local name (e.g. name of a local element), a global name (with namespace, if non-chameleon schema), a generated name for an anonymous type
        // for the rest of the method, we assume a non-chameleon schema

        // bookmark-refs must only be placed if the QName refers to something that is printed elsewhere

        if (qname.getLocalPart().isEmpty()) {
            // this is a pseudo-name, e.g.from a pseudo group for sequences of elements or attributes
            // i.e.implementation specifics for uniform interfaces
            return run.text("");
        }
        if (XMLConstants.NULL_NS_URI.equals(qname.getNamespaceURI())) {
            //  this is a locally-scoped name, e.g. a locally-scoped element name or a locally-scoped attribute name
            //  NOT in this category: element-ref, group-ref, attribute-ref, attributeGroup-ref, global declarations
            //  this MUST NOT be prefixed
            //  this MUST NOT be given a bookmark-thingy
            return dumbPrettyPrint(run, format, qname, prependSymbol, false, false);
        }

        // at this point, qname is really a qname, so it has to be in the concepts-map
        final NamedConceptWithOrigin concept = context.getConcept(qname);

        if (context.dependencyHelper.getLocalConceptNames().contains(qname)) {
            // local concepts SHOULD NOT be prefixed except for situations where it is needed for disambiguation: attribute/element ref
            if (concept instanceof Attributes.GlobalAttributeDeclaration || concept instanceof Element) {
                // this is a REF to a global, internal attribute or element
                // this SHOULD be prefixed
                // this COULD be given a bookmark-thingy
                return dumbPrettyPrint(run, format, qname, prependSymbol, true, true);
            } else {
                // if this is an enumeration, consider inlining it
                if ((Config.INLINE_ENUMS && considerInlining) && concept instanceof Type.Simple.Enumeration) {
                    final List<Type.Simple.Enumeration.Value> enumValues = ((Type.Simple.Enumeration) concept).getEnumValues();
                    final String valuesString = enumValues.stream().map(Type.Simple.Enumeration.Value::getValue).collect(Collectors.joining(" | "));
                    if (valuesString.length() < 200) {
                        run.text(valuesString);
                        return run;
                    }
                }
                // this is the name of a global, internal concept where we don't need the prefix
                // this SHOULD NOT be prefixed
                // this COULD be given a bookmark-thingy
                return dumbPrettyPrint(run, format, qname, prependSymbol, false, true);
            }
        } else {
            // this is a TYPE or a ref to a FOREIGN GlobalAttributeDeclaration or a ref to a FOREIGN Element
            // this MUST be prefixed
            // this MUST NOT be given a bookmark-thingy
            return dumbPrettyPrint(run, format, qname, prependSymbol, true, false);
        }
    }

    protected void setGridSpan(XWPFTableRow row, int col, int colSpan) {
        final XWPFTableCell cell = row.getCell(col);
        final CTTc cellCore = cell.getCTTc();
        final CTTcPr cellProps = Optional.ofNullable(cellCore.getTcPr())
                                         .orElseGet(cellCore::addNewTcPr);
        final CTDecimalNumber gridSpan = Optional.ofNullable(cellProps.getGridSpan())
                                                 .orElseGet(cellProps::addNewGridSpan);
        gridSpan.setVal(BigInteger.valueOf(colSpan));
    }

    protected void insertDocumentation(final ParagraphHelper.RunHelper runHelper, final Iterable<String> description) {
        final Iterator<String> iterator = description.iterator();
        if (iterator.hasNext()) {
            runHelper.text(iterator.next());
        }
        while (iterator.hasNext()) {
            runHelper.lineBreak().text(iterator.next());
        }
    }

    @RequiredArgsConstructor
    public enum Format {
        NORMAL(false, false), BOLD(true, false), ITALICS(false, true), BOLD_ITALICS(true, true);
        final boolean bold, italics;

        public Format combine(final Format other) {
            if (bold || other.bold) {
                if (italics || other.italics) {
                    return BOLD_ITALICS;
                }
                return BOLD;
            }
            if (italics || other.italics) {
                return ITALICS;
            }
            return NORMAL;
        }

        public void applyTo(final XWPFRun run) {
            run.setFontSize(8);
            if (bold) {
                run.setBold(true);
            }
            if (italics) {
                run.setItalic(true);
            }
        }

        public void applyTo(final CTParaRPr pRunProps) {
            Optional.ofNullable(pRunProps.getSz())
                    .orElseGet(pRunProps::addNewSz)
                    .setVal(Constants.fontSizeToHpsMeasure(8));
            if (bold) {
                pRunProps.addNewB().setVal(STOnOff.TRUE);
            }
            if (italics) {
                pRunProps.addNewI().setVal(STOnOff.TRUE);
            }
        }

        public void applyTo(final CTR run) {
            final CTRPr runProps = Optional.ofNullable(run.getRPr())
                                           .orElseGet(run::addNewRPr);
            Optional.ofNullable(runProps.getSz())
                    .orElseGet(runProps::addNewSz)
                    .setVal(Constants.fontSizeToHpsMeasure(8));
            if (bold) {
                runProps.addNewB().setVal(STOnOff.TRUE);
            }
            if (italics) {
                runProps.addNewI().setVal(STOnOff.TRUE);
            }
        }
    }

    public ParagraphHelper.RunHelper createCellHelper(final XWPFTableRow row, final int pos, final Format style) {
        final XWPFParagraph paragraph = getOrCreateCell(row, pos).getParagraphArray(0);
        {
            final CTP pCore = paragraph.getCTP();
            final CTPPr pProps = Optional.ofNullable(pCore.getPPr()).orElseGet(pCore::addNewPPr);
            setMinimalSpacing(pProps);
            final CTParaRPr pRunProps = Optional.ofNullable(pProps.getRPr()).orElseGet(pProps::addNewRPr);
            style.applyTo(pRunProps);
        }
        return new ParagraphHelper.RunHelper(paragraph, run -> {
            style.applyTo(run);
            return run;
        });
    }

    public XWPFTableCell getOrCreateCell(XWPFTableRow row, int pos) {
        XWPFTableCell possCell;
        {
            while (null == (possCell = row.getCell(pos))) {
                row.addNewTableCell();
            }
        }
        return possCell;
    }

    protected void setMinimalSpacing(final CTPPr pProps) {
        final CTSpacing spacing = Optional.ofNullable(pProps.getSpacing()).orElseGet(pProps::addNewSpacing);
        // <w:spacing w:after="60" w:before="60" w:line="190" w:lineRule="atLeast"/>
        spacing.setAfter(BigInteger.valueOf(60));
        spacing.setBefore(BigInteger.valueOf(60));
        spacing.setLine(BigInteger.valueOf(190));
        spacing.setLineRule(STLineSpacingRule.AT_LEAST);
    }

    public static void addColumnToGrid(CTTblGrid tableGrid, double widthInCm) {
        tableGrid.addNewGridCol().setW(cmToTwipsAsBI(widthInCm));
    }

    public static BigInteger cmToTwipsAsBI(double widthInCm) {
        return BigInteger.valueOf(Double.valueOf(
                widthInCm * Constants.TWIPS_PER_CM
        ).longValue());
    }

    public void setCellWidth(int col, double columnWidth) {
        final CTTblWidth tblWidth = table.getRow(0).getCell(col).getCTTc().addNewTcPr().addNewTcW();
        tblWidth.setW(cmToTwipsAsBI(columnWidth));
        tblWidth.setType(STTblWidth.DXA);
    }

    public void mergeCellsVertically(int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            final XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if (rowIndex == fromRow) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    @Deprecated
    public void mergeCellsHorizontally(int row, int fromCol, int toCol) {
        mergeCellsHorizontally(table.getRow(row), fromCol, toCol);
    }

    @Deprecated
    public void mergeCellsHorizontally(XWPFTableRow tableRow, int fromCol, int toCol) {
        for (int colIndex = fromCol; colIndex <= toCol; colIndex++) {
            final XWPFTableCell cell = tableRow.getCell(colIndex);
            if (colIndex == fromCol) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }
}
