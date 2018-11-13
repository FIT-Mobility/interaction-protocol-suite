package de.fraunhofer.fit.omp.reportgenerator.playground;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.DependencyAnalyzer;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.BookmarkHelper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.BookmarkRegistry;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.CaptionHelper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.Context;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.CursorHelper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.DataTypeTableHelper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.OperationsTableHelper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.ParagraphHelper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.PrefixHelper;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi.VdvStyle;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;

import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public class poiPlayground {

    @Value
    private static class Operation {
        @NonNull final String name;
        @NonNull final String reqDT;
        @NonNull final String respDT;
    }

    @Value
    @RequiredArgsConstructor
    private static class DataType {
        @NonNull final QName name;

        DataType(final String name) {
            this(new QName(name));
        }
    }

    private static final List<Operation> OPERATIONS = Lists.newArrayList(
            new Operation("GetAllData", "-", "PassengerCountingService.GetAllDataResponse"),
            new Operation("SubscribeAllData", "SubscribeRequest", "SubscribeResponse"),
            new Operation("UnsubscribeAllData", "UnsubscribeRequest", "UnsubscribeResponse"),
            new Operation("RetrieveSpecificDoorData",
                    "PassengerCountingService.RetrieveSpecificDoorDataRequest",
                    "PassengerCountingService.RetrieveSpecificDoorDataResponse"),
            new Operation("SetCounterData",
                    "PassengerCountingService.SetCounterDataRequest",
                    "DataAcceptedResponse")
    );

    private static final LinkedHashMap<QName, DataType> DATA_TYPES
            = new LinkedHashMap<>(Maps.uniqueIndex(Lists.newArrayList(
            new DataType("PassengerCountingService.RetrieveSpecificDoorDataRequest"),
            new DataType("PassengerCountingService.RetrieveSpecificDoorDataResponse"),
            new DataType("PassengerCountingService.SpecificDoorData")
    ), DataType::getName));

    public static void main(String[] args) {
        final String inputfile = "src/test/resources/playground-template.docx";
        final String outputDocx = "target/playground-poi-template-instance.docx";
        try (final XWPFDocument document = new XWPFDocument(new FileInputStream(inputfile))) {

            printParagraphs(document);
            printTables(document);
            printStyles(document);

            deleteAllParagraphsAfterString(document, "Dienst PassengerCountingService");

            try (final CursorHelper cursorHelper = new CursorHelper(document, document.createParagraph().getCTP().newCursor())) {

                final PrefixHelper prefixHelper = new PrefixHelper(document.getPackagePart());
                final BookmarkRegistry bookmarkRegistry = new BookmarkRegistry();
                final ParagraphHelper paragraphHelper = new ParagraphHelper(cursorHelper);
                final CaptionHelper captionHelper = new CaptionHelper(cursorHelper, bookmarkRegistry);

                final Context context = new Context(prefixHelper, bookmarkRegistry, document, null, new DependencyAnalyzer.DependencyHelper(DATA_TYPES.keySet(), null, null));

                paragraphHelper.createHeading(VdvStyle.HEADING_1,
                        "Dienst PassengerCountingService");

                /* Aufgaben des Dienstes und die Nutzung */

                paragraphHelper.createHeading(VdvStyle.HEADING_2,
                        "Aufgaben des Dienstes und die Nutzung");

                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                               .text("Der Fahrgastzähldienst stellt die Daten bereit, die von automatischen " +
                                       "Fahrgastzählsystemen (AFZS) erzeugt werden. Es können mehrere Instanzen " +
                                       "dieses Dienstes vorhanden sein. Insbesondere sind folgende Konfigurationen zu " +
                                       "berücksichtigen:");

                {
                    final ParagraphHelper.ListHelper bulletListHelper = paragraphHelper.createBulletListHelper();

                    bulletListHelper.createRunHelper()
                                    .text("Ein einzelner Fahrgastzähldienst stellt die Daten für alle Türen des " +
                                            "Fahrzeugs bereit.");
                    bulletListHelper.createRunHelper()
                                    .text("Für jede Tür des Fahrzeugs ist ein separater Fahrgastzähldienst vorhanden.");

                }

                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                               .lineBreak()
                               .text("Für Applikationen des Fahrgastzähldienstes gibt es 2 verschiedene Verfahren zur " +
                                       "Behandlung der AFZS-Daten:");

                {
                    final ParagraphHelper.ListHelper numberedListHelper = paragraphHelper.createNumberedListHelper();

                    numberedListHelper.createRunHelper()
                                      .text("Verfahren")
                                      .lineBreak()
                                      .text("Operationen „SubscribeAllData“ und „UnsubscribeAllData“,")
                                      .lineBreak()
                                      .text("Datenstruktur „PassengerCountingService.AllData“")
                                      .lineBreak()
                                      .text("Die Zähler „In“ und „Out“ werden nur inkrementiert. Es gibt kein " +
                                              "Rücksetzen dieser Zähler und sie können überlaufen. Es gibt 3 Optionen" +
                                              " zum Auslösen einer Zähleraktualisierung:");
                    {
                        final ParagraphHelper.ListHelper lvl2helper = numberedListHelper.increaseIndent();
                        lvl2helper.createRunHelper()
                                  .text("Aktualisierung bei jeder Erhöhung der Zähler „In“ oder „Out“ oder in einem " +
                                          "festen Zeitintervall, z. B. alle 5 Sekunden")
                                  .lineBreak()
                                  .text("Um diese Option zu nutzen, braucht das AFZS keine zusätzlichen Informationen.");
                        lvl2helper.createRunHelper()
                                  .text("Aktualisierung beim Abschluss des Zählens nach einem Türschließen")
                                  .lineBreak()
                                  .text("Erfordert, dass das AFZS über den Türzustand informiert ist.");
                        lvl2helper.createRunHelper()
                                  .text("Aktualisierung beim Verlassen einer Haltestelle")
                                  .lineBreak()
                                  .text("Erfordert, dass das AFZS über die Fahrzeugbewegung informiert ist " +
                                          "(Odometer-Daten, GPS-Daten).");
                    }
                    paragraphHelper.createSilentListHelper()
                                   .createRunHelper()
                                   .text("Alternativ kann die Operation „GetAllData“ verwendet werden, um die Zähldaten " +
                                           "bei Bedarf abzufragen. Die Rückgabestruktur enthält dabei immer die " +
                                           "aktuellen Daten.")
                                   .lineBreak();

                    numberedListHelper.createRunHelper()
                                      .text("Verfahren")
                                      .lineBreak()
                                      .text("Operationen „RetrieveSpecificDoorData“ und „SetCounterData“,")
                                      .lineBreak()
                                      .text("Datenstruktur „PassengerCountingService.SpecificDoorData“")
                                      .lineBreak()
                                      .text("Die Applikation fragt die Zähldaten beim Verlassen der Haltestelle ab und " +
                                              "setzt sie nach erfolgreichem Empfang zurück. Die Zählwerte „In“ und " +
                                              "„Out“ stellen folglich die Fahrgastzahlen für eine Haltestelle dar. Es " +
                                              "ist erforderlich, dass die Applikation über das Verlassen von " +
                                              "Haltestellen informiert ist. Dieses Verfahren kann nur dann angewandt " +
                                              "werden, wenn ausschließlich EINE Applikation den Fahrgastzähldienst nutzt" +
                                              ". Ansonsten würde das Rücksetzen der Zähler zu Dateninkonsistenzen " +
                                              "zwischen den verschiedenen Applikationen führen.")
                                      .pageBreak();
                }

                /* Tasks of the Service and its Usage */

                paragraphHelper.createEnHeading(VdvStyle.BERSCHRIFT_ENGLISCH, "Tasks of the Service and its Usage");

                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                               .text("Passenger counting service is used to provide data generated by automatic passenger" +
                                       " counting systems (APCS). This service may exist in multiple instances. " +
                                       "Especially the following configurations are considered:");

                {
                    final ParagraphHelper.ListHelper bulletListHelper = paragraphHelper.createBulletListHelper();

                    bulletListHelper.createRunHelper()
                                    .text("Single passenger counting service is used to provide data for all doors of " +
                                            "vehicle");
                    bulletListHelper.createRunHelper()
                                    .text("Individual passenger counting service is used for each door of vehicle");
                }

                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                               .lineBreak()
                               .text("There are two different approaches for an application to handle APCS data:");

                {
                    final ParagraphHelper.ListHelper numberedListHelper = paragraphHelper.createNumberedListHelper();

                    numberedListHelper.createRunHelper()
                                      .text("Approach")
                                      .lineBreak()
                                      .text("Operations “SubscribeAllData” and “UnsubscribeAllData”,")
                                      .lineBreak()
                                      .text("data structure “PassengerCountingService.AllData”")
                                      .lineBreak()
                                      .text("Count values “In” and “Out” are incremented only. There is no reset of these" +
                                              " values and they may overrun. There are three options to trigger count " +
                                              "value update:");
                    {
                        final ParagraphHelper.ListHelper lvl2helper = numberedListHelper.increaseIndent();
                        lvl2helper.createRunHelper()
                                  .text("Update on every increment of count values “In” or “Out” or in fixed intervals (e" +
                                          ".g. every 5 seconds)")
                                  .lineBreak()
                                  .text("APCS does not require any additional information to use this option.");
                        lvl2helper.createRunHelper()
                                  .text("Update on count finish after door closing")
                                  .lineBreak()
                                  .text("Requires that the APCS is able to identify the door state");
                        lvl2helper.createRunHelper()
                                  .text("Update on leaving of stop")
                                  .lineBreak()
                                  .text("Requires that the APC has information about the vehicle movements (odometer " +
                                          "data, GPS data etc.)");
                    }
                    paragraphHelper.createSilentListHelper()
                                   .createRunHelper()
                                   .text("Alternatively operation “GetAllData” may be used to query data as needed. " +
                                           "Response data structure always contains current data.")
                                   .lineBreak();

                    numberedListHelper.createRunHelper()
                                      .text("Approach")
                                      .lineBreak()
                                      .text("Operations “RetrieveSpecificDoorData” and “SetCounterData”,")
                                      .lineBreak()
                                      .text("data structure “PassengerCountingService.SpecificDoorData”")
                                      .lineBreak()
                                      .text("Application queries count data on leaving of stop and reset it after " +
                                              "successful receiving. As a result count values “In” and “Out” represent " +
                                              "passenger counts for a stop. It is required that the application is able " +
                                              "to identify stop departures. This approach should only be chosen if there " +
                                              "definitely exists only ONE consuming application for this service as the " +
                                              "reset of counts may lead to unintended interferences between multiple " +
                                              "consumers of count data.");
                }

                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                               .lineBreak()
                               .text("Each application shall use one of the alternative approaches exclusively.")
                               .pageBreak();

                /* Operations of the PassengerCountingService */

                paragraphHelper.createHeading(VdvStyle.HEADING_2,
                        "Operations of the PassengerCountingService");

                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                               .text("The ")
                               .code("PassengerCountingService")
                               .text(" supports the following operations (cf. ")
                               .bookmarkRef(new BookmarkHelper("PassengerCountingService"), BookmarkHelper::toFloatLabel)
                               .text("). The description of the structures is placed afterwards in chapters 1.3ff");


                captionHelper.createTableCaption(
                        "Description of Operations of ",
                        new BookmarkHelper("PassengerCountingService"),
                        ""
                );
                final OperationsTableHelper operationsTableHelper = new OperationsTableHelper(context, cursorHelper);
                for (Operation operation : OPERATIONS) {
                    operationsTableHelper.addOperation(operation.getName(), new QName(operation.getReqDT()), new QName(operation.getRespDT()));
                }

                paragraphHelper.createHeading(VdvStyle.HEADING_2, "Operation RetrieveSpecificDoorData");

                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                               .text("To retrieve counting information from a specific door this operation makes is " +
                                       "possible.");

                paragraphHelper.createHeading(VdvStyle.HEADING_3, "Request");

                captionHelper.createTableCaption(
                        "Description of ",
                        new BookmarkHelper("PassengerCountingService.RetrieveSpecificDoorDataRequest"),
                        ""
                );

                {
                    final DataTypeTableHelper dataTypeTableHelper = new DataTypeTableHelper(context, cursorHelper,
                            new QName("PassengerCountingService.RetrieveSpecificDoorDataRequest"),
                            Collections.singletonList("Request structure for the specific door information counting data")
                    );
                    try (final DataTypeTableHelper.GroupHelper groupHelper = dataTypeTableHelper.startGroup(new QName(""))) {
                        groupHelper.addElement(
                                "1:1",
                                new QName("DoorID"),
//                                DataTypeTableHelper.DataTypePrefix.NONE,
                                new QName("IBIS-IP.NMTOKEN"),
                                Collections.singletonList("ID for door identification")
                        );
                    }
                }

                paragraphHelper.createHeading(VdvStyle.HEADING_3, "Response");

                captionHelper.createTableCaption(
                        "Description of ",
                        new BookmarkHelper("PassengerCountingService.RetrieveSpecificDoorDataResponse"),
                        ""
                );

                {
                    final DataTypeTableHelper dataTypeTableHelper = new DataTypeTableHelper(context, cursorHelper,
                            new QName("PassengerCountingService.RetrieveSpecificDoorDataResponse"),
                            Collections.singletonList("Response structure with counting data of a specific door"));
                    try (final DataTypeTableHelper.GroupHelper groupHelper = dataTypeTableHelper.startGroup(new QName(""))) {
                        try (final DataTypeTableHelper.GroupHelper.ChoiceHelper choiceHelper = groupHelper.startChoice("1:1")) {
                            choiceHelper.addOption(new QName("SpecificDoorData"),
//                                DataTypeTableHelper.DataTypePrefix.PLUS,
                                    new QName("PassengerCountingService.SpecificDoorData"),
                                    Collections.singletonList("Detailed structure with counting data of a specific door (cf. table below)")
                            );
                            choiceHelper.addOption(new QName("OperationErrorMessage"),
//                                DataTypeTableHelper.DataTypePrefix.NONE,
                                    new QName("IBIS-IP.string"),
                                    Collections.singletonList("Error Message")
                            );

                        }
                    }
                }

                captionHelper.createTableCaption(
                        "Description of ",
                        new BookmarkHelper("PassengerCountingService.SpecificDoorData"),
                        ""
                );

                {
                    final DataTypeTableHelper dataTypeTableHelper = new DataTypeTableHelper(context, cursorHelper,
                            new QName("PassengerCountingService.SpecificDoorData"),
                            Collections.singletonList("Detailed structure with counting data of a specific door"));
                    try (final DataTypeTableHelper.GroupHelper groupHelper = dataTypeTableHelper.startGroup(new QName(""))) {
                        groupHelper.addElement(
                                "1:1",
                                new QName("TimeStamp"),
//                                DataTypeTableHelper.DataTypePrefix.NONE,
                                new QName("IBIS-IP.datetime"),
                                Collections.singletonList("Time stamp of the response")
                        );
                        groupHelper.addElement(
                                "1:1",
                                new QName("CountingData"),
//                                DataTypeTableHelper.DataTypePrefix.PLUS,
                                new QName("DoorInformation"),
                                Collections.singletonList("Detailed structure with counting data and additional information of a specific door (cf. chapter 1.22 VDV301-2-1)")
                        );
                    }
                }
            }
            {
                final XWPFParagraph paragraph = document.createParagraph();
                paragraph.createRun().setText("text before hyperlink ");
                appendExternalHyperlink("http://www.heise.de", "myLink", paragraph);
                paragraph.createRun().setText(" text after hyperlink");
            }
            try (final FileOutputStream outputStream = new FileOutputStream(outputDocx)) {
                document.write(outputStream);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends an external hyperlink to the paragraph.
     *
     * @param url       The URL to the external target
     * @param text      The linked text
     * @param paragraph the paragraph the link will be appended to.
     */
    public static void appendExternalHyperlink(String url, String text, XWPFParagraph paragraph) {
        //Add the link as External relationship
        String id = paragraph.getDocument().getPackagePart().addExternalRelationship(url, XWPFRelation.HYPERLINK.getRelation()).getId();

        //Append the link and bind it to the relationship
        CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
        cLink.setId(id);
        cLink.addNewR().addNewT().setStringValue(text);
    }

    private static void deleteAllParagraphsAfterString(final XWPFDocument document, final String searchString) {
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
        while (i < paragraphsSize) {
            document.removeBodyElement(document.getPosOfParagraph(paragraphs.get(i++)));
        }
    }


    private static void printStyles(XWPFDocument document) throws IOException {
        try {
            final XWPFStyles styles = document.getStyles();
            final CTStyles style = document.getStyle();
            for (CTStyle ctStyle : style.getStyleArray()) {
                final String styleId = ctStyle.getStyleId();
                final XWPFStyle xwpfStyle = styles.getStyle(styleId);
                System.out.println("StyleID " + styleId + ": "
                        + Optional.ofNullable(xwpfStyle).map(XWPFStyle::getName).orElse(""));
            }
        } catch (final XmlException e) {
            e.printStackTrace();
        }
    }

    private static void printTables(XWPFDocument document) {
        int tableCounter = 0;
        for (final XWPFTable table : document.getTables()) {
            System.out.println("table " + tableCounter++ + ": " + table.getText());
            for (final XWPFTableRow row : table.getRows()) {
                for (final XWPFTableCell cell : row.getTableCells()) {
                    for (final XWPFParagraph paragraph : cell.getParagraphs()) {
                        for (final XWPFRun run : paragraph.getRuns()) {
                            System.out.println(run.text());
                        }
                    }
                }
            }
        }
    }

    private static void printParagraphs(XWPFDocument document) {
        int paragraphCounter = 0;
        for (final XWPFParagraph paragraph : document.getParagraphs()) {
            System.out.println("paragraph " + paragraphCounter++ + ": " + paragraph.getParagraphText());
            int runCounter = 0;
            for (final XWPFRun run : paragraph.getRuns()) {
                System.out.println("run " + paragraphCounter + "." + runCounter++ + ": " + run.text());
            }
        }
    }

}
