package de.fraunhofer.fit.omp.reportgenerator.converter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.fraunhofer.fit.omp.reportgenerator.exception.RuntimeJsonException;
import de.fraunhofer.fit.omp.reportgenerator.model.ReportContext;
import de.fraunhofer.fit.omp.reportgenerator.model.ReportContextImpl;
import de.fraunhofer.fit.omp.model.json.OmpToolProjectSchema;
import de.fraunhofer.fit.omp.reportgenerator.model.template.Function;
import de.fraunhofer.fit.omp.reportgenerator.model.template.Project;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Schema;
import de.fraunhofer.fit.omp.reportgenerator.reporter.xsd.parser.XSDParser;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.template.IContext;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fabian Ohler <ohler@dbis.rwth-aachen.de>
 * @since 01.12.2017
 */
public class JsonDataConverter3 implements ModelConverter {

    private static final String PROJECT = "project";

    @Override
    public ReportContext getContext(IXDocReport report, String jsonAsString) throws Exception {
        final OmpToolProjectSchema projectSchema;
        try {
            projectSchema = JsonObjectMapper.INSTANCE.get().readValue(jsonAsString, OmpToolProjectSchema.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new RuntimeJsonException(e);
        }

        final String xsd = projectSchema.getSchema().getXsd();

        final Schema xsdSchema = createSchema(projectSchema.getSchema().getBaseURI(), xsd, projectSchema.getFunctions());

        final HashMap<String, Object> contextMap = new HashMap<>();
        final Project project = Project.convert(projectSchema, xsdSchema.getOperations());
        contextMap.put(PROJECT, project);

        IContext iContext = report.createContext(contextMap);
        return new ReportContextImpl(iContext, xsdSchema);
    }

    private Schema createSchema(@Nullable final URI baseURI, final String xsd,
                                final List<de.fraunhofer.fit.omp.model.json.Function> functions) {
        return XSDParser.createFromData(baseURI, xsd)
                        .process((elements, typeNameToType) -> functions.stream().map(f -> Function.convert(f, elements, typeNameToType)).collect(Collectors.toMap(Function::getName, java.util.function.Function.identity())));
    }
}
