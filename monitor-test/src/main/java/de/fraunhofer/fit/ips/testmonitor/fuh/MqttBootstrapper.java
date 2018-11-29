package de.fraunhofer.fit.ips.testmonitor.fuh;

import com.google.common.collect.Maps;
import com.saxonica.xqj.SaxonXQDataSource;
import de.fraunhofer.fit.ips.model.simple.Function;
import de.fraunhofer.fit.ips.model.simple.Project;
import de.fraunhofer.fit.ips.model.simple.Service;
import de.fraunhofer.fit.ips.testmonitor.configuration.MqttConfiguration;
import de.fraunhofer.fit.ips.testmonitor.reporting.Reporter;
import de.fraunhofer.fit.ips.testmonitor.routing.mqtt.message.MqttMessageExchangeHandlerFactory;
import de.fraunhofer.fit.ips.testmonitor.routing.mqtt.message.MqttMessageFunctionInfo;
import de.fraunhofer.fit.ips.testmonitor.routing.mqtt.requestreply.MqttRequestReplyExchangeHandlerFactory;
import de.fraunhofer.fit.ips.testmonitor.routing.mqtt.requestreply.MqttRequestReplyFunctionInfo;
import de.fraunhofer.fit.ips.testmonitor.validation.FunctionValidator;
import de.fraunhofer.fit.ips.testmonitor.validation.InstanceValidator;
import de.fraunhofer.fit.ips.testmonitor.data.DataExtractor;
import de.fraunhofer.fit.ips.testmonitor.topic.ListTopicMatcher;
import de.fraunhofer.fit.ips.testmonitor.topic.TopicMatcher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class MqttBootstrapper {

    private static final String RESPONSE_SUFFIX = "Response";
    private static final String REQUEST_SUFFIX = "Request";

    final MqttMessageExchangeHandlerFactory mqttMessageExchangeHandlerFactory;
    final MqttRequestReplyExchangeHandlerFactory mqttRequestReplyExchangeHandlerFactory;

    public static MqttBootstrapper bootstrap(final Reporter reporter, final MqttConfiguration config)
            throws IOException, SAXException, XQException, TransformerException, ParserConfigurationException {
        final List<URL> schemaFiles;
        try (final DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(config.getXsdDirectory()), "*.xsd")) {
            schemaFiles = StreamSupport.stream(paths.spliterator(), false)
                                       .map(path -> {
                                           try {
                                               return path.toUri().toURL();
                                           } catch (final MalformedURLException e) {
                                               throw new RuntimeException(e);
                                           }
                                       })
                                       .collect(Collectors.toList());
        }
        if (schemaFiles.isEmpty()) {
            log.error("NO SCHEMA FILES FOUND IN " + config.getXsdDirectory());
        }

        final Map<QName, ElementAndTopics> elementAndTopicsMap = extractValidTopicsMap(schemaFiles);
        final InstanceValidator dataTypeValidator = new InstanceValidator(reporter, schemaFiles);

        final MqttMessageExchangeHandlerFactory mqttMessageExchangeHandlerFactory = new MqttMessageExchangeHandlerFactory(dataTypeValidator,
                Maps.transformValues(elementAndTopicsMap,
                        eat -> MqttMessageFunctionInfo.builder()
                                                      .functionElementName(eat.elementName)
                                                      .functionName(eat.elementName.getLocalPart())
                                                      .serviceName("common")
                                                      .topicValidator(new ListTopicMatcher(eat.validTopics))
                                                      .build()
                )::get
        );

        final Map<URL, Set<QName>> fileToElements = extractElementsPerFile(schemaFiles);

        final Map<URL, Service> fileToFunctions = Maps.transformValues(fileToElements, elements ->
                elements.stream()
                        .map(potentialResponse -> {
                            final String localName = potentialResponse.getLocalPart();
                            if (!localName.endsWith(RESPONSE_SUFFIX)) {
                                return null;
                            }
                            final String corePart = localName.substring(0, localName.length() - RESPONSE_SUFFIX.length());
                            final String namespaceURI = potentialResponse.getNamespaceURI();
                            final QName request = new QName(namespaceURI, corePart + REQUEST_SUFFIX);
                            if (!elements.contains(request)) {
                                return null;
                            }
                            // FIXME: dirty request / response QNames point to elements instead of types, is used as elements below
                            return new Function(corePart, request, potentialResponse, new ArrayList<>());
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.collectingAndThen(Collectors.toList(), functions -> new Service("FuH-Service", functions)))
        );

        final Map<QName, MqttRequestReplyFunctionInfo> lookup = new HashMap<>();

        for (final Map.Entry<URL, Service> entry : fileToFunctions.entrySet()) {
            final URL dataTypeSchemaFile = entry.getKey();
            final Service dummyService = entry.getValue();
            final String functionSchema = DataExtractor.generateFunctionSchema(new StreamSource(dataTypeSchemaFile.toString()), new Project("FuH", Collections.singletonList(dummyService)), null);
            final FunctionValidator functionValidator = new FunctionValidator(reporter, null, Paths.get(config.getXsdDirectory(), "function-schema.xsd").toUri().toString(), functionSchema);
            for (final Function function : dummyService.getFunctions()) {
                final QName inputElementName = function.getRequestType();
                lookup.put(
                        inputElementName,
                        MqttRequestReplyFunctionInfo.builder()
                                                    .functionElementName(new QName(inputElementName.getNamespaceURI(), function.getName()))
                                                    .functionName(function.getName())
                                                    .serviceName("common")
                                                    .functionValidator(functionValidator)
                                                    .requestTopicValidator(createTopicMatcher(elementAndTopicsMap, inputElementName))
                                                    .replyTopicValidator(createTopicMatcher(elementAndTopicsMap, function.getResponseType()))
                                                    .build()
                );
            }
        }

        final MqttRequestReplyExchangeHandlerFactory mqttRequestReplyExchangeHandlerFactory
                = new MqttRequestReplyExchangeHandlerFactory(reporter, dataTypeValidator, lookup::get);

        log.info("extracted {} elements from the XSD files, {} of them annotated with valid-path meta infos; matched {} functions.",
                fileToElements.values().stream().mapToInt(Set::size).sum(),
                elementAndTopicsMap.size(),
                lookup.size()
        );

        return new MqttBootstrapper(mqttMessageExchangeHandlerFactory, mqttRequestReplyExchangeHandlerFactory);
    }

    protected static TopicMatcher createTopicMatcher(Map<QName, ElementAndTopics> elementAndTopicsMap,
                                                     QName elementName) {
        final ElementAndTopics elementAndTopics = elementAndTopicsMap.get(elementName);
        if (null == elementAndTopics) {
            return ALWAYS_MATCH;
        }
        final List<String> validTopics = elementAndTopics.validTopics;
        if (null == validTopics || validTopics.isEmpty()) {
            return ALWAYS_MATCH;
        }
        return new ListTopicMatcher(validTopics);
    }

    static final TopicMatcher ALWAYS_MATCH = (message, topic) -> {
    };

    @RequiredArgsConstructor
    private static class AutoCloser implements AutoCloseable {
        final XQConnection conn;

        @Override
        public void close() throws XQException {
            conn.close();
        }
    }

    @Getter
    @RequiredArgsConstructor
    static class ElementAndTopics {
        final QName elementName;
        final List<String> validTopics;
    }

    private static Map<QName, ElementAndTopics> extractValidTopicsMap(final Collection<URL> files)
            throws XQException {
        final Map<QName, ElementAndTopics> map = new HashMap<>();
        final XQDataSource dataSource = new SaxonXQDataSource();
        final XQConnection conn = dataSource.getConnection();
        try (final AutoCloser ignored = new AutoCloser(conn)) {
            final XQPreparedExpression expression = conn.prepareExpression("declare namespace xs=\"http://www.w3.org/2001/XMLSchema\";\n" +
                    "declare namespace in=\"http://www.dimo-fuh.de/internal\";\n" +
                    "declare variable $doc external;\n" +
                    "for $schema in $doc/xs:schema\n" +
                    "    for $element in $schema/xs:element\n" +
                    "        for $topic in $element/xs:annotation/xs:appinfo/in:ValidTopics/in:ValidTopic\n" +
                    "            return fn:concat( '{', data($schema/@targetNamespace) , '}', data($element/@name), ':', data($topic/@topic))");
            for (final URL file : files) {
                expression.bindDocument(new QName("doc"), new StreamSource(file.toExternalForm()), null);
                final XQResultSequence resultSequence = expression.executeQuery();
                while (resultSequence.next()) {
                    final String itemAsString = resultSequence.getItemAsString(null);
                    final int colonIndex = itemAsString.indexOf(':', itemAsString.indexOf('}'));
                    final QName elementName = QName.valueOf(itemAsString.substring(0, colonIndex));
                    final String validTopic = itemAsString.substring(colonIndex + 1);
                    map.computeIfAbsent(elementName, x -> new ElementAndTopics(x, new ArrayList<>()))
                       .getValidTopics()
                       .add(validTopic);
                }
            }
        }
        return map;
    }

    private static Map<URL, Set<QName>> extractElementsPerFile(final Collection<URL> files)
            throws XQException {
        final Map<URL, Set<QName>> map = new HashMap<>();
        final XQDataSource dataSource = new SaxonXQDataSource();
        final XQConnection conn = dataSource.getConnection();
        try (final AutoCloser ignored = new AutoCloser(conn)) {
            final XQPreparedExpression expression = conn.prepareExpression("declare namespace xs=\"http://www.w3.org/2001/XMLSchema\";\n" +
                    "declare variable $doc external;\n" +
                    "for $schema in $doc/xs:schema\n" +
                    "    for $element in $schema/xs:element\n" +
                    "        return fn:concat( '{', data($schema/@targetNamespace) , '}', data($element/@name))");
            for (final URL file : files) {
                expression.bindDocument(new QName("doc"), new StreamSource(file.toExternalForm()), null);
                final XQResultSequence resultSequence = expression.executeQuery();
                while (resultSequence.next()) {
                    final String itemAsString = resultSequence.getItemAsString(null);
                    final QName elementName = QName.valueOf(itemAsString);
                    map.computeIfAbsent(file, x -> new HashSet<>())
                       .add(elementName);
                }
            }
        }
        return map;
    }
}
