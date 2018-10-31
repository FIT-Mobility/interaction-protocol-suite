package de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.poi;

import de.fraunhofer.fit.omp.reportgenerator.model.template.Function;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.NamedConceptWithOrigin;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.DependencyAnalyzer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
public class Context {
    final PrefixHelper prefixHelper;
    final BookmarkRegistry bookmarkRegistry;
    final XWPFDocument document;
    final Schema schema;
    final DependencyAnalyzer.DependencyHelper dependencyHelper;

    public Map<String, Function> getSchemaOperations() {
        return schema.getOperations();
    }

    public @Nullable
    NamedConceptWithOrigin getConcept(final QName conceptName) {
        return schema.getConcepts().get(conceptName);
    }
}
