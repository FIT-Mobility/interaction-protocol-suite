package de.fraunhofer.fit.ips.testmonitor.data;

import de.fraunhofer.fit.ips.model.Converter;
import de.fraunhofer.fit.ips.model.IllegalDocumentStructureException;
import de.fraunhofer.fit.ips.model.simple.Project;
import de.fraunhofer.fit.ips.proto.javabackend.SchemaAndProjectStructure;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.ips.testmonitor.routing.messagebased.MessageBasedFunctionInfo;
import de.fraunhofer.fit.ips.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.ips.testmonitor.validation.InstanceValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ValidatorFactory {
    private static final String DATATYPE_SCHEMA_SYSTEM_ID = "4122f46f-3dd7-4739-b7eb-24d34bb5f6b8.xsd";
    private static final String FUNCTION_SCHEMA_SYSTEM_ID = "function-schema.xsd";

    @Getter
    @RequiredArgsConstructor
    public static class RegularOperations {
        final InstanceValidator instanceValidator;
        final FunctionValidator functionValidator;
        final HashMap<QName, MessageBasedFunctionInfo> lookup;
    }

    @Getter
    @RequiredArgsConstructor
    public static class VaasOperations {
        final InstanceValidator instanceValidator;
        final HashMap<QName, MessageBasedFunctionInfo> lookup;
    }

    public static RegularOperations newRegularOperations(final Reporter reporter,
                                                         final URI protoSchemaAndProjectStructureURI)
            throws IOException, TransformerException, ParserConfigurationException, SAXException, IllegalDocumentStructureException {
        final CommonFields commonFields = CommonFields.create(protoSchemaAndProjectStructureURI);
        final InstanceValidator instanceValidator = new InstanceValidator(reporter, DATATYPE_SCHEMA_SYSTEM_ID, commonFields.dataTypeSchema);
        final FunctionValidator functionValidator = new FunctionValidator(reporter, commonFields.resourceResolver, FUNCTION_SCHEMA_SYSTEM_ID, commonFields.functionSchema);
        return new RegularOperations(instanceValidator, functionValidator, commonFields.lookup);
    }

    public static VaasOperations newVaasOperations(final Reporter reporter,
                                                   final URI protoSchemaAndProjectStructureURI)
            throws IOException, TransformerException, ParserConfigurationException, SAXException, IllegalDocumentStructureException {
        final CommonFields commonFields = CommonFields.create(protoSchemaAndProjectStructureURI);
        final InstanceValidator functionValidator = new InstanceValidator(reporter, commonFields.resourceResolver, FUNCTION_SCHEMA_SYSTEM_ID, commonFields.functionSchema);
        return new VaasOperations(functionValidator, commonFields.lookup);
    }

    @RequiredArgsConstructor
    private static class CommonFields {
        final String dataTypeSchema;
        final String functionSchema;
        final HashMap<QName, MessageBasedFunctionInfo> lookup;
        final LSResourceResolver resourceResolver;

        protected static CommonFields create(final URI protoSchemaAndProjectStructureURI)
                throws IOException, ParserConfigurationException, TransformerException, IllegalDocumentStructureException {
            final String dataTypeSchema;
            final String functionSchema;
            final HashMap<QName, MessageBasedFunctionInfo> lookup = new HashMap<>();
            {
                final Project project;
                try (final InputStream inputStream = protoSchemaAndProjectStructureURI.toURL().openStream()) {
                    final SchemaAndProjectStructure schemaAndProjectStructure = SchemaAndProjectStructure.parseDelimitedFrom(inputStream);
                    dataTypeSchema = schemaAndProjectStructure.getSchema().getXsd();
                    project = Converter.convert(schemaAndProjectStructure.getProject());
                }

                functionSchema = DataExtractor.generateFunctionSchema(
                        new StreamSource(new StringReader(dataTypeSchema), DATATYPE_SCHEMA_SYSTEM_ID),
                        project,
                        functionInfo -> lookup.put(functionInfo.getRequestElementName(), functionInfo));
            }

            final LSResourceResolver resourceResolver = (final String type, final String namespaceURI, final String publicId,
                                                         final String systemId, final String baseURI) -> {
                if (DATATYPE_SCHEMA_SYSTEM_ID.equals(systemId)) {
                    return new DOMInputImpl(publicId, systemId, baseURI, dataTypeSchema, null);
                }
                try {
                    return new DOMInputImpl(publicId, systemId, baseURI, new URI(baseURI).resolve(systemId).toURL().openStream(), null);
                } catch (final IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            };
            return new CommonFields(dataTypeSchema, functionSchema, lookup, resourceResolver);
        }
    }
}
