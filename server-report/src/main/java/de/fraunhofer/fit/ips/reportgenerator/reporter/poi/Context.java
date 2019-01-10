package de.fraunhofer.fit.ips.reportgenerator.reporter.poi;

import de.fraunhofer.fit.ips.model.template.Project;
import de.fraunhofer.fit.ips.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.ips.model.xsd.Schema;
import de.fraunhofer.fit.ips.reportgenerator.reporter.ReportConfiguration;
import de.fraunhofer.fit.ips.reportgenerator.reporter.Reporter;
import lombok.Getter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.Set;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
public class Context {
    final Schema schema;
    final Project project;
    final ReportConfiguration reportConfiguration;
    final Set<QName> assignedParticles;
    final XWPFDocument document;
    final PrefixHelper prefixHelper;
    final BookmarkRegistry bookmarkRegistry;
    final Reporter.RichtextMarkerManager richtextMarkerManager = new Reporter.RichtextMarkerManager();

    public Context(final Schema schema,
                   final Project project,
                   final ReportConfiguration reportConfiguration,
                   final Set<QName> assignedParticles,
                   final XWPFDocument document) {
        this.schema = schema;
        this.project = project;
        this.reportConfiguration = reportConfiguration;
        this.assignedParticles = assignedParticles;
        this.document = document;
        this.prefixHelper = new PrefixHelper(document.getPackagePart());
        this.bookmarkRegistry = new BookmarkRegistry();
    }

    public @Nullable
    NamedConceptWithOrigin getConcept(final QName conceptName) {
        return schema.getConcepts().get(conceptName);
    }

    public CursorHelper newCursorHelper(final XWPFParagraph paragraph) {
        return CursorHelper.fromParagraph(document, paragraph);
    }
}
