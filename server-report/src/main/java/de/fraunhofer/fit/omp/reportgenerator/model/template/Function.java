package de.fraunhofer.fit.omp.reportgenerator.model.template;

import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Element;
import de.fraunhofer.fit.omp.reportgenerator.model.xsd.Type;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Slf4j
@Builder
public class Function {
    @NonNull final String name;
    @NonNull final Documentation documentation;
    @Nullable final Type.Complex inputDataType;
    @Nullable final Type.Complex outputDataType;

    public static Function convert(final de.fraunhofer.fit.omp.model.json.Function from,
                                   final Map<QName, Element> elementNameToElement,
                                   final Map<QName, Type.Complex> typeNameToType) {
        final String functionName = from.getNcname();
        final Type.Complex resolvedInput = getType(functionName, from.getInputElementName(), elementNameToElement, typeNameToType);
        final Type.Complex resolvedOutput = getType(functionName, from.getOutputElementName(), elementNameToElement, typeNameToType);
        return Function.builder()
                       .name(functionName)
                       .documentation(Documentation.convert(from.getDocumentation()))
                       .inputDataType(resolvedInput)
                       .outputDataType(resolvedOutput)
                       .build();
    }

    private static Type.Complex getType(final String functionName,
                                        @Nullable final de.fraunhofer.fit.omp.model.json.QName paramElementName,
                                        final Map<QName, Element> elementNameToElement,
                                        final Map<QName, Type.Complex> typeNameToType) {
        if (null == paramElementName) {
            return null;
        }
        final QName elementName = new QName(paramElementName.getNamespaceuri().toString(), paramElementName.getNcname());
        final Element element = elementNameToElement.get(elementName);
        if (null == element) {
            log.warn("could not resolve element name of parameter of {}: {}. Defined types were: {}", functionName, elementName, elementNameToElement.keySet());
            return null;
        }
        final Type.Complex resolvedInput = typeNameToType.get(element.getDataType());
        if (null == resolvedInput) {
            log.warn("could not resolve parameter of {}: {}. Defined types were: {}", functionName, element, typeNameToType.keySet());
        }
        return resolvedInput;
    }
}
