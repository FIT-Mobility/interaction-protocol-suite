package de.fraunhofer.fit.ips.reportgenerator.playground;

import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.xsd.Documentations;
import de.fraunhofer.fit.ips.model.xsd.Element;
import de.fraunhofer.fit.ips.model.xsd.Origin;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import de.fraunhofer.fit.ips.model.xsd.Type;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class XsdParser2Poi {
//
//    private static final List<de.fraunhofer.fit.ips.model.json.Function> OPERATIONS = Lists.newArrayList(
//            newFunction("GetAllData", "-", "CustomerInformationService.GetAllDataResponse"),
//            newFunction("GetVehicleData", "-", "CustomerInformationService.GetVehicleDataResponse")
//    );
//
//    private static de.fraunhofer.fit.ips.model.json.Function newFunction(final String name,
//                                                                                         final String inputElementName,
//                                                                                         final String outputElementName) {
//        final de.fraunhofer.fit.ips.model.json.Function function = new de.fraunhofer.fit.ips.model.json.Function();
//        function.setNcname(name);
//        wrapQName(function::setInputElementName, inputElementName);
//        wrapQName(function::setOutputElementName, outputElementName);
//        final de.fraunhofer.fit.ips.model.json.Documentation documentation = new de.fraunhofer.fit.ips.model.json.Documentation();
//        documentation.setEnglish("en-test");
//        documentation.setGerman("de-test");
//        function.setDocumentation(documentation);
//        return function;
//    }
//
//    private static void wrapQName(Consumer<de.fraunhofer.fit.ips.model.json.QName> elementNameSetter,
//                                  String outputElementName) {
//        final de.fraunhofer.fit.ips.model.json.QName qName = new de.fraunhofer.fit.ips.model.json.QName();
//        qName.setNcname(outputElementName);
//        qName.setNamespaceuri(URI.create(""));
//        elementNameSetter.accept(qName);
//    }
//
//    public static void main(String[] args) {
//        final String inputfile = "src/test/resources/playground-template.docx";
//        final String outputDocx = "src/test/resources/xsdParser2Poi-instance.docx";
//
//        final XSDParser xsdParser = XSDParser.createFromUri("src/test/resources/xsd/IBIS-IP_CustomerInformationService_V1.0.xsd");
//        final Schema schema = xsdParser.process((elements, complexTypes) -> {
//
//            final Map<String, Function> operationsMap = new HashMap<>();
//
//            final Map<QName, Type.Complex> typeNameToType
//                    = complexTypes.values()
//                                  .stream()
//                                  .collect(Collectors.toMap(
//                                          Type.Complex::getName,
//                                          java.util.function.Function.identity()));
//
//            // we fake the elements since they are contained in the main XSD file
//            for (de.fraunhofer.fit.ips.model.json.Function operation : OPERATIONS) {
//                final Element inputElement = createDummyElement(operation.getInputElementName());
//                final Element outputElement = createDummyElement(operation.getOutputElementName());
//                final ImmutableMap<QName, Element> elementNameToElement = ImmutableMap.of(
//                        inputElement.getName(), inputElement,
//                        outputElement.getName(), outputElement
//                );
//                final Function function = Function.convert(operation, elementNameToElement, typeNameToType);
//                operationsMap.put(function.getName(), function);
//            }
//            return operationsMap;
//        });
//
//        final Collection<Function> operations = schema.getOperations().values();
//        final DependencyAnalyzer.DependencyHelper dependencyHelper = DependencyAnalyzer.analyze(schema);
//
//        try (final XWPFDocument document = new XWPFDocument(new FileInputStream(inputfile))) {
//            deleteAllParagraphsAfterString(document, "Dienst PassengerCountingService");
//            try (final CursorHelper cursorHelper = new CursorHelper(document, document.createParagraph().getCTP().newCursor())) {
//
//                final PrefixHelper prefixHelper = new PrefixHelper(document.getPackagePart());
//                final BookmarkRegistry bookmarkRegistry = new BookmarkRegistry();
//                final ParagraphHelper paragraphHelper = new ParagraphHelper(cursorHelper);
//                final CaptionHelper captionHelper = new CaptionHelper(cursorHelper, bookmarkRegistry);
//
//                final Context context = new Context(prefixHelper, bookmarkRegistry, document, schema, dependencyHelper);
//
//                // start on a fresh page
//                paragraphHelper.createRunHelper(VdvStyle.NORMAL).pageBreak();
//
//                /* Operations of the PassengerCountingService */
//
//                paragraphHelper.createHeading(VdvStyle.HEADING_2,
//                        "Operations of the PassengerCountingService");
//
//                paragraphHelper.createRunHelper(VdvStyle.NORMAL)
//                               .text("The ")
//                               .code("PassengerCountingService")
//                               .text(" supports the following operations (cf. ")
//                               .bookmarkRef(new BookmarkHelper("PassengerCountingService"), BookmarkHelper::toFloatLabel)
//                               .text("). The description of the structures is placed afterwards in chapters 1.3ff");
//
//                captionHelper.createTableCaption(
//                        "Description of Operations of ",
//                        new BookmarkHelper("PassengerCountingService"),
//                        ""
//                );
//                final OperationsTableHelper operationsTableHelper = new OperationsTableHelper(context, cursorHelper);
//                for (final Function operation : operations) {
//                    operationsTableHelper.addOperation(
//                            operation.getName(),
//                            Optional.ofNullable(operation.getInputDataType()).map(Type.Complex::getName).orElse(new QName("")),
//                            Optional.ofNullable(operation.getOutputDataType()).map(Type.Complex::getName).orElse(new QName("")));
//                }
//
//                for (final Function operation : operations) {
//                    handleFunctionWithHeading(context, cursorHelper, paragraphHelper, captionHelper, operation);
//                }
//
//                document.enforceUpdateFields();
//
//            }
//            try (final FileOutputStream outputStream = new FileOutputStream(outputDocx)) {
//                document.write(outputStream);
//            }
//        } catch (final IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static Element createDummyElement(
//            final de.fraunhofer.fit.ips.model.json.QName elementName) {
//        final QName qName = new QName(elementName.getNcname());
//        return new Element(qName, new QName(qName.getLocalPart() + "Structure"), "0:1", new Origin(
//                true, false, "src/test/resources/xsd/IBIS-IP_CustomerInformationService_V1.0.xsd"
//        ), new Documentations());
//    }
//
//    static void handleFunctionWithHeading(final Context context,
//                                          final CursorHelper cursorHelper,
//                                          final ParagraphHelper paragraphHelper,
//                                          final CaptionHelper captionHelper,
//                                          final Function operation) {
//        paragraphHelper.createHeading(VdvStyle.HEADING_2, "Function " + operation.getName());
//
//
//        paragraphHelper.createRunHelper(VdvStyle.NORMAL)
//                       .text("add documentation of the operation here");
//
//        paragraphHelper.createHeading(VdvStyle.HEADING_3, "Request");
//
//        VdvTables.processDataTypeAndDependencies(context, cursorHelper, captionHelper, operation.getInputDataType());
//
//        paragraphHelper.createHeading(VdvStyle.HEADING_3, "Response");
//
//        VdvTables.processDataTypeAndDependencies(context, cursorHelper, captionHelper, operation.getOutputDataType());
//    }
//
//    private static void deleteAllParagraphsAfterString(final XWPFDocument document, final String searchString) {
//        final ImmutableList<XWPFParagraph> paragraphs = ImmutableList.copyOf(document.getParagraphs());
//        final PositionInParagraph startPos = new PositionInParagraph();
//        final int paragraphsSize = paragraphs.size();
//        int i = 0;
//        while (i < paragraphsSize) {
//            if (null != paragraphs.get(i).searchText(searchString, startPos)) {
//                break;
//            }
//            ++i;
//        }
//        while (i < paragraphsSize) {
//            document.removeBodyElement(document.getPosOfParagraph(paragraphs.get(i++)));
//        }
//    }
}
