package de.fraunhofer.fit.omp.testmonitor.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.fit.omp.model.json.OmpToolProjectSchema;
import de.fraunhofer.fit.omp.model.json.Service;
import de.fraunhofer.fit.omp.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.omp.testmonitor.routing.messagebased.MessageBasedFunctionInfo;
import de.fraunhofer.fit.omp.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.omp.testmonitor.validation.InstanceValidator;
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
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
                                                         final URI jsonURI)
            throws IOException, TransformerException, ParserConfigurationException, SAXException {
        final CommonFields commonFields = CommonFields.create(jsonURI);
        final InstanceValidator instanceValidator = new InstanceValidator(reporter, commonFields.givenBaseURI + DATATYPE_SCHEMA_SYSTEM_ID, commonFields.dataTypeSchema);
        final FunctionValidator functionValidator = new FunctionValidator(reporter, commonFields.resourceResolver, commonFields.givenBaseURI + FUNCTION_SCHEMA_SYSTEM_ID, commonFields.functionSchema);
        return new RegularOperations(instanceValidator, functionValidator, commonFields.lookup);
    }

    public static VaasOperations newVaasOperations(final Reporter reporter,
                                                   final URI jsonURI)
            throws IOException, TransformerException, ParserConfigurationException, SAXException {
        final CommonFields commonFields = CommonFields.create(jsonURI);
        final InstanceValidator functionValidator = new InstanceValidator(reporter, commonFields.resourceResolver, commonFields.givenBaseURI + FUNCTION_SCHEMA_SYSTEM_ID, commonFields.functionSchema);
        return new VaasOperations(functionValidator, commonFields.lookup);
    }

    @RequiredArgsConstructor
    private static class CommonFields {
        final String dataTypeSchema;
        final String functionSchema;
        final HashMap<QName, MessageBasedFunctionInfo> lookup;
        final String givenBaseURI;
        final LSResourceResolver resourceResolver;

        protected static CommonFields create(final URI jsonURI)
                throws IOException, ParserConfigurationException, TransformerException {
            final String dataTypeSchema;
            final String functionSchema;
            final URI givenBaseURI;
            final HashMap<QName, MessageBasedFunctionInfo> lookup = new HashMap<>();
            {
                final ObjectMapper objectMapper = new ObjectMapper();
                final OmpToolProjectSchema ompToolProjectSchema = objectMapper.readValue(jsonURI.toURL(), OmpToolProjectSchema.class);
                dataTypeSchema = ompToolProjectSchema.getSchema().getXsd();
                givenBaseURI = ompToolProjectSchema.getSchema().getBaseURI();

                functionSchema = DataExtractor.generateFunctionSchema(
                        new StreamSource(new StringReader(dataTypeSchema), DATATYPE_SCHEMA_SYSTEM_ID),
                        ompToolProjectSchema.getFunctions().stream().map(FunctionData::fromJSON).collect(Collectors.toList()),
                        getFunction2ServiceLookup(ompToolProjectSchema)::get,
                        functionInfo -> lookup.put(functionInfo.getRequestElementName(), functionInfo));
            }

            final String sanitizedURI = sanitizeURI(givenBaseURI);
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
            return new CommonFields(dataTypeSchema, functionSchema, lookup, sanitizedURI, resourceResolver);
        }

        private static Map<String, String> getFunction2ServiceLookup(OmpToolProjectSchema ompToolProjectSchema) {
            final Map<String, String> function2Service = new HashMap<>();
            for (final Service service : ompToolProjectSchema.getServices()) {
                for (final String function : service.getFunctions()) {
                    final String previousValue = function2Service.put(function, service.getName());
                    if (null != previousValue) {
                        log.error("function {} belongs to more than one service: {} & {}", function, service.getName(), previousValue);
                    }
                }
            }
            return function2Service;
        }
    }

    protected static String sanitizeURI(final URI givenBaseURI) {
        if (null == givenBaseURI) {
            return "";
        }
        final URI normalizedBaseURI = givenBaseURI.normalize();
        if (normalizedBaseURI.isAbsolute()) {
            return sanitizeURIHelper(normalizedBaseURI);
        }
        log.warn("BASE URI ({}) SHOULD BE ABSOLUTE, WILL TRY RANDOM THINGS NOW!", givenBaseURI);
        // base URI should be absolute, this is for playing around locally, only
        return sanitizeURIHelper(Paths.get(".").toAbsolutePath().resolve(normalizedBaseURI.toString()).normalize().toUri());
    }

    private static String sanitizeURIHelper(final URI uri) {
        final String asd = uri.toString();
        if (asd.endsWith("/")) {
            return asd;
        }
        log.warn("BASE URI ({}) SHOULD END WITH /", uri);
        return asd + "/";
    }
}
