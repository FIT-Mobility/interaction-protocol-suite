package de.fraunhofer.fit.ips.reportgenerator.reporter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import de.fraunhofer.fit.ips.model.template.Assertion;
import de.fraunhofer.fit.ips.model.template.Function;
import de.fraunhofer.fit.ips.model.template.Level;
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
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.BookmarkHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.CaptionHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.Context;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.CursorHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.ParagraphHelper;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.VdvStyle;
import de.fraunhofer.fit.ips.reportgenerator.reporter.poi.VdvTables;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
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
            for (final StructureBase child : context.getProject().getChildren()) {
                child.accept(new MyStructureVisitor(context, cursorHelper, 1));
            }
        } finally {
            context.getDocument().removeBodyElement(context.getDocument().getPosOfParagraph(paragraph));
        }
    }

    @RequiredArgsConstructor
    private static class MyStructureVisitor implements StructureVisitor {
        final Context context;
        final CursorHelper cursorHelper;
        final int oneBasedDepth;

        private <T extends InnerNode> void createHeadingAndHandleChildren(final T innerNode,
                                                                          final boolean suppressNumbering,
                                                                          final Runnable specialPart) {
            final ParagraphHelper paragraphHelper = new ParagraphHelper(cursorHelper);

            final List<String> languages = context.getReportConfiguration().getLanguages();
            final Map<String, String> languageToHeading = innerNode.getHeadingTitle().getLanguageToPlaintext();

            final Iterator<String> setLanguages = Iterators.filter(languages.iterator(), languageToHeading::containsKey);
            if (!setLanguages.hasNext()) {
                // FIXME log / warn ?!
                return;
            }
            final String primaryLanguage = setLanguages.next();

            paragraphHelper.createHeading(VdvStyle.getHeadingLevel(oneBasedDepth, true), languageToHeading.get(primaryLanguage), suppressNumbering);

            specialPart.run();

            final PrimaryLanguageVisitor primaryLanguageVisitor = new PrimaryLanguageVisitor(context, cursorHelper, paragraphHelper, primaryLanguage);
            final Iterator<StructureBase> childIterator = innerNode.getChildren().iterator();
            while (childIterator.hasNext() && !primaryLanguageVisitor.innerNodeFound) {
                childIterator.next().accept(primaryLanguageVisitor);
            }
            final List<StructureBase> leavesFound = primaryLanguageVisitor.leavesFound;
            while (setLanguages.hasNext()) {
                final String additionalLanguage = setLanguages.next();
                paragraphHelper.createEnHeading(VdvStyle.getHeadingLevel(oneBasedDepth, false), languageToHeading.get(additionalLanguage), suppressNumbering);
                final AdditionalLanguageVisitor additionalLanguageVisitor = new AdditionalLanguageVisitor(context, cursorHelper, paragraphHelper, additionalLanguage);
                for (final StructureBase structureBase : leavesFound) {
                    structureBase.accept(additionalLanguageVisitor);
                }
            }
            // iterate over the rest
            if (childIterator.hasNext()) {
                final MyStructureVisitor oneDeeper = new MyStructureVisitor(context, cursorHelper, 1 + oneBasedDepth);
                while (childIterator.hasNext()) {
                    childIterator.next().accept(oneDeeper);
                }
            }
        }

        @Override
        public void visit(final Assertion assertion) {
            // FIXME render assertions
        }

        @Override
        public void visit(final Particle datatype) {
        }

        @Override
        public void visit(final Function function) {
            createHeadingAndHandleChildren(function, false, () -> {
                // TODO are there special function parts? if yes, print here
            });
        }

        @Override
        public void visit(final Level level) {
            createHeadingAndHandleChildren(level, level.isSuppressNumbering(), () -> {
            });
        }

        @Override
        public void visit(final Project project) {
        }

        @Override
        public void visit(final Request request) {
            createHeadingAndHandleChildren(request, false, () -> {
                // TODO are there special request parts? if yes, print here
            });
        }

        @Override
        public void visit(final Response response) {
            createHeadingAndHandleChildren(response, false, () -> {
                // TODO are there special response parts? if yes, print here
            });
        }

        @Override
        public void visit(final Service service) {
            createHeadingAndHandleChildren(service, false, () -> {
                // TODO are there special service parts? if yes, print here
            });
        }

        @Override
        public void visit(final Text text) {
        }
    }

    private static void placeMarkerForRichtext(final Context context, final ParagraphHelper paragraphHelper,
                                               final Text text, final String language) {
        final String richtext = text.getRtContent().getLanguageToRichtext().get(language);
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
        public void visit(final Particle datatype) {
            leavesFound.add(datatype);
            final CaptionHelper captionHelper = new CaptionHelper(cursorHelper, context.getBookmarkRegistry());
            final NamedConceptWithOrigin concept = context.getConcept(datatype.getName());
            if (null == concept) {
                throw new RuntimeException("could not find datatype " + datatype.getName());
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

                @Override
                public void visit(final Type.Simple.Restriction restriction) {
                    // TODO render simple types
                }

                @Override
                public void visit(final Type.Simple.List list) {
                    // TODO render simple types
                }

                @Override
                public void visit(final Type.Simple.Union union) {
                    // TODO render simple types
                }

                @Override
                public void visit(final Attributes.GlobalAttributeGroupDeclaration globalAttributeGroupDeclaration) {
                }

                @Override
                public void visit(final Attributes.GlobalAttributeDeclaration globalAttributeDeclaration) {
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
