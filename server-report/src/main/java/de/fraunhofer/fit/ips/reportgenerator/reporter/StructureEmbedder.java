package de.fraunhofer.fit.ips.reportgenerator.reporter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import de.fraunhofer.fit.ips.model.template.Assertion;
import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.template.Level;
import de.fraunhofer.fit.ips.model.template.MultilingualRichtext;
import de.fraunhofer.fit.ips.model.template.Particle;
import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.template.Request;
import de.fraunhofer.fit.ips.model.template.Response;
import de.fraunhofer.fit.ips.model.template.Service;
import de.fraunhofer.fit.ips.model.template.Text;
import de.fraunhofer.fit.ips.model.template.helper.InnerNode;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;
import de.fraunhofer.fit.ips.model.xsd.Attributes;
import de.fraunhofer.fit.ips.model.xsd.Element;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOriginVisitor;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import de.fraunhofer.fit.ips.model.xsd.Type;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.AttributesTableHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.BookmarkHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.CaptionHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.Context;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.CursorHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.ParagraphHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.SimpleTypesTableHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.VdvStyle;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.VdvTables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
public class StructureEmbedder {
    public static Reporter.RichtextMarkerManager embed(final Schema schema, final Project project,
                                                       final ReportConfiguration reportConfiguration,
                                                       final XWPFDocument document) {
        final Context context = new Context(schema, project, reportConfiguration, document);

        final PositionInParagraph pseudoStartPos = new PositionInParagraph();
        for (final XWPFParagraph paragraph : ImmutableList.copyOf(document.getParagraphs())) {
            if (null != paragraph.searchText("$$$PREFACE$$$", pseudoStartPos)) {
                replaceParagraphWithPreface(context, paragraph);
            }
            if (null != paragraph.searchText("$$$MAIN_PART$$$", pseudoStartPos)) {
                replaceParagraphWithMainPart(context, paragraph);
            }
        }
//        document.enforceUpdateFields();
        return context.getRichtextMarkerManager();
    }

    private static void replaceParagraphWithPreface(final Context context, final XWPFParagraph paragraph) {
        try (final CursorHelper cursorHelper = context.newCursorHelper(paragraph)) {
            // TODO insert preface
        } finally {
            context.getDocument().removeBodyElement(context.getDocument().getPosOfParagraph(paragraph));
        }
    }

    private static void replaceParagraphWithMainPart(final Context context, final XWPFParagraph paragraph) {
        try (final CursorHelper cursorHelper = context.newCursorHelper(paragraph)) {
            final MyStructureVisitor structureVisitor = new MyStructureVisitor(context, cursorHelper, new ParagraphHelper(cursorHelper), 1);
            for (final StructureBase child : context.getProject().getChildren()) {
                child.accept(structureVisitor);
            }
        } finally {
            context.getDocument().removeBodyElement(context.getDocument().getPosOfParagraph(paragraph));
        }
    }

    @RequiredArgsConstructor
    private static class MyStructureVisitor implements StructureVisitor {
        final Context context;
        final CursorHelper cursorHelper;
        final ParagraphHelper paragraphHelper;
        final int oneBasedDepth;

        interface SpecialPartEmbedder {
            void embed(final String language, final boolean primary);
        }

        private <T extends InnerNode> void createHeadingAndHandleChildren(final T innerNode,
                                                                          final boolean suppressNumbering,
                                                                          final SpecialPartEmbedder specialPart) {
            final List<String> languages = context.getReportConfiguration().getLanguages();
            final Map<String, String> languageToHeading = innerNode.getHeadingTitle().getLanguageToPlaintext();

            final Iterator<String> setLanguages = Iterators.filter(languages.iterator(), languageToHeading::containsKey);
            if (!setLanguages.hasNext()) {
                // FIXME log / warn ?!
                log.error("no heading set for element " + innerNode.toString());
                return;
            }
            final String primaryLanguage = setLanguages.next();

            paragraphHelper.createHeading(VdvStyle.getHeadingLevel(oneBasedDepth, true), languageToHeading.get(primaryLanguage), suppressNumbering);

            specialPart.embed(primaryLanguage, true);

            final PrimaryLanguageVisitor primaryLanguageVisitor = new PrimaryLanguageVisitor(context, cursorHelper, paragraphHelper, primaryLanguage);
            final ListIterator<StructureBase> childIterator = innerNode.getChildren().listIterator();
            while (childIterator.hasNext()) {
                childIterator.next().accept(primaryLanguageVisitor);
                if (primaryLanguageVisitor.innerNodeFound) {
                    childIterator.previous();
                    break;
                }
            }
            final List<StructureBase> leavesFound = primaryLanguageVisitor.leavesFound;
            while (setLanguages.hasNext()) {
                final String additionalLanguage = setLanguages.next();
                paragraphHelper.createEnHeading(VdvStyle.getHeadingLevel(oneBasedDepth, false), languageToHeading.get(additionalLanguage), suppressNumbering);

                specialPart.embed(additionalLanguage, false);

                final AdditionalLanguageVisitor additionalLanguageVisitor = new AdditionalLanguageVisitor(context, cursorHelper, paragraphHelper, additionalLanguage);
                for (final StructureBase structureBase : leavesFound) {
                    structureBase.accept(additionalLanguageVisitor);
                }
            }
            // iterate over the rest
            if (childIterator.hasNext()) {
                final MyStructureVisitor oneDeeper = new MyStructureVisitor(context, cursorHelper, paragraphHelper, 1 + oneBasedDepth);
                while (childIterator.hasNext()) {
                    childIterator.next().accept(oneDeeper);
                }
            }
        }

        @Override
        public void visit(final Assertion assertion) {
            final String assertionID = UUID.randomUUID().toString();
            createHeadingAndHandleChildren(assertion, assertion.isSuppressNumbering(), (language, primary) -> {
                // render ml-doc
                placeMarkerForRichtext(context, paragraphHelper, assertion.getDescription(), language);
                if (primary) {
                    // render test as referencable float

                    // FIXME implement rendering test as referencable float

                    final ParagraphHelper.RunHelper runHelper = paragraphHelper.createRunHelper(VdvStyle.BLOCK_TEXT);
                    runHelper.code(assertion.getTest());
                    if (null != assertion.getXpathDefaultNamespace()) {
                        runHelper.lineBreak();
                        runHelper.code("xpathDefaultNamespace: ").code(assertion.getXpathDefaultNamespace());
                    }
                    final BookmarkHelper bookmarkHelper = new BookmarkHelper(assertionID, assertion.getHeadingTitle().getLanguageToPlaintext().get(language));
                    final CaptionHelper captionHelper = new CaptionHelper(cursorHelper, context.getBookmarkRegistry());
                    captionHelper.createAssertionCaption("", bookmarkHelper, "");
                } else {
                    // render reference to test-float
                    // FIXME: strings should be language-dependent
                    final BookmarkHelper bookmarkHelper = new BookmarkHelper(assertionID);
                    paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                                   .text("Assertion ")
                                   .bookmarkRef(bookmarkHelper, BookmarkHelper::toConceptLabel)
                                   .text(" is defined in ")
                                   .bookmarkRef(bookmarkHelper, BookmarkHelper::toFloatLabel);
                }
            });
        }

        @Override
        public void visit(final Particle datatype) {
        }

        @Override
        public void visit(final Function function) {
            createHeadingAndHandleChildren(function, false, (language, primary) -> {
                // TODO are there special function parts? if yes, print here
            });
        }

        @Override
        public void visit(final Level level) {
            createHeadingAndHandleChildren(level, level.isSuppressNumbering(), (language, primary) -> {
            });
        }

        @Override
        public void visit(final Project project) {
        }

        @Override
        public void visit(final Request request) {
            createHeadingAndHandleChildren(request, false, (language, primary) -> {
                // TODO are there special request parts? if yes, print here
            });
        }

        @Override
        public void visit(final Response response) {
            createHeadingAndHandleChildren(response, false, (language, primary) -> {
                // TODO are there special response parts? if yes, print here
            });
        }

        @Override
        public void visit(final Service service) {
            createHeadingAndHandleChildren(service, false, (language, primary) -> {
                // TODO are there special service parts? if yes, print here
            });
        }

        @Override
        public void visit(final Text text) {
        }
    }

    private static void placeMarkerForRichtext(final Context context, final ParagraphHelper paragraphHelper,
                                               final Text text, final String language) {
        placeMarkerForRichtext(context, paragraphHelper, text.getRtContent(), language);
    }

    private static void placeMarkerForRichtext(final Context context, final ParagraphHelper paragraphHelper,
                                               final MultilingualRichtext multilingualRichtext, final String language) {
        final String richtext = multilingualRichtext.getLanguageToRichtext().get(language);
        final String marker = context.getRichtextMarkerManager().newMarkerForRichtext(richtext);
        paragraphHelper.createRunHelper(VdvStyle.NORMAL).mergefield(marker);
    }

    @RequiredArgsConstructor
    private static class PrimaryLanguageVisitor implements DefaultStructureVisitor {
        final Context context;
        final CursorHelper cursorHelper;
        final ParagraphHelper paragraphHelper;
        final List<StructureBase> leavesFound = new ArrayList<>();
        final String primaryLanguage;
        boolean innerNodeFound = false;

        @Override
        public void visit(final Text text) {
            leavesFound.add(text);
            placeMarkerForRichtext(context, paragraphHelper, text, primaryLanguage);
        }

        @Override
        public void visit(final Particle particle) {
            leavesFound.add(particle);
            final CaptionHelper captionHelper = new CaptionHelper(cursorHelper, context.getBookmarkRegistry());
            final NamedConceptWithOrigin concept = context.getConcept(particle.getName());
            if (null == concept) {
                throw new RuntimeException("could not find particle " + particle.getName());
            }
            concept.accept(new NamedConceptWithOriginVisitor() {
                @Override
                public void visit(final Element element) {
                }

                @Override
                public void visit(final Type.Group group) {
                    VdvTables.processGroupDataType(context, cursorHelper, captionHelper, group);
                }

                @Override
                public void visit(final Type.Complex complex) {
                    VdvTables.processComplexDataType(context, cursorHelper, captionHelper, complex);
                }

                @Override
                public void visit(final Type.Simple.Enumeration enumeration) {
                    VdvTables.processEnumeration(context, cursorHelper, captionHelper, enumeration);
                }

                // TODO really render simple types separately?
                @Override
                public void visit(final Type.Simple.Restriction restriction) {
                    final String name = restriction.getName().getLocalPart();
                    captionHelper.createTableCaption(
                            "Description of restriction ",
                            new BookmarkHelper("tbl_" + name, name),
                            ""
                    );
                    final SimpleTypesTableHelper simpleTypesTableHelper = new SimpleTypesTableHelper(context, cursorHelper);
                    simpleTypesTableHelper.addRestriction(restriction);
                }

                @Override
                public void visit(final Type.Simple.List list) {
                    final String name = list.getName().getLocalPart();
                    captionHelper.createTableCaption(
                            "Description of list ",
                            new BookmarkHelper("tbl_" + name, name),
                            ""
                    );
                    final SimpleTypesTableHelper simpleTypesTableHelper = new SimpleTypesTableHelper(context, cursorHelper);
                    simpleTypesTableHelper.addList(list);
                }

                @Override
                public void visit(final Type.Simple.Union union) {
                    final String name = union.getName().getLocalPart();
                    captionHelper.createTableCaption(
                            "Description of union ",
                            new BookmarkHelper("tbl_" + name, name),
                            ""
                    );
                    final SimpleTypesTableHelper simpleTypesTableHelper = new SimpleTypesTableHelper(context, cursorHelper);
                    simpleTypesTableHelper.addUnion(union);
                }

                @Override
                public void visit(final Attributes.GlobalAttributeGroupDeclaration globalAttributeGroupDeclaration) {
                }

                @Override
                public void visit(final Attributes.GlobalAttributeDeclaration globalAttributeDeclaration) {
                    final String name = globalAttributeDeclaration.getName().getLocalPart();
                    captionHelper.createTableCaption(
                            "Description of global attribute ",
                            new BookmarkHelper("tbl_" + name, name),
                            ""
                    );
                    final AttributesTableHelper attributesTableHelper = new AttributesTableHelper(context, cursorHelper);
                    attributesTableHelper.addAttributeDeclaration(context, globalAttributeDeclaration);
                }
            });
        }

        @Override
        public void fallback(final StructureBase structureBase) {
            innerNodeFound = true;
        }
    }

    @RequiredArgsConstructor
    private static class AdditionalLanguageVisitor implements DefaultStructureVisitor {
        final Context context;
        final CursorHelper cursorHelper;
        final ParagraphHelper paragraphHelper;
        final String language;

        @Override
        public void visit(final Text text) {
            placeMarkerForRichtext(context, paragraphHelper, text, language);
        }

        @Override
        public void visit(final Particle datatype) {
            final BookmarkHelper bookmarkHelper = new BookmarkHelper(datatype.getName().getLocalPart());
            // FIXME: strings should be language-dependent
            paragraphHelper.createRunHelper(VdvStyle.NORMAL)
                           .text("Data type ")
                           .bookmarkRef(bookmarkHelper, BookmarkHelper::toConceptLabel)
                           .text(" is defined in ")
                           .bookmarkRef(bookmarkHelper, BookmarkHelper::toFloatLabel);
        }

        @Override
        public void fallback(final StructureBase structureBase) {
        }
    }

    interface DefaultStructureVisitor extends StructureVisitor {
        void fallback(final StructureBase structureBase);

        default void visit(final Assertion assertion) {
            fallback(assertion);
        }

        default void visit(final Particle datatype) {
            fallback(datatype);
        }

        default void visit(final Function function) {
            fallback(function);
        }

        default void visit(final Level level) {
            fallback(level);
        }

        default void visit(final Project project) {
            fallback(project);
        }

        default void visit(final Request request) {
            fallback(request);
        }

        default void visit(final Response response) {
            fallback(response);
        }

        default void visit(final Service service) {
            fallback(service);
        }

        default void visit(final Text text) {
            fallback(text);
        }
    }
}
