package de.fraunhofer.fit.ips.model.template;

import de.fraunhofer.fit.ips.model.template.helper.InnerNode;
import de.fraunhofer.fit.ips.model.template.helper.StructureBase;
import de.fraunhofer.fit.ips.model.template.helper.StructureVisitor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@RequiredArgsConstructor
@Getter
@Slf4j
@Builder
public class Function implements InnerNode {
    @Nonnull final String name;
    @Nonnull final MultilingualPlaintext headingTitle;
    @Nonnull final List<StructureBase> children;
    @Nullable final Request request;
    @Nullable final Response response;
    @Nonnull final List<Assertion> assertions;

    @Override
    public void accept(final StructureVisitor visitor) {
        visitor.visit(this);
    }

//    private static Type.Complex getType(final String functionName,
//                                        @Nullable final de.fraunhofer.fit.ips.model.json.QName paramElementName,
//                                        final Map<QName, Element> elementNameToElement,
//                                        final Map<QName, Type.Complex> typeNameToType) {
//        if (null == paramElementName) {
//            return null;
//        }
//        final QName elementName = new QName(paramElementName.getNamespaceuri().toString(), paramElementName.getNcname());
//        final Element element = elementNameToElement.get(elementName);
//        if (null == element) {
//            log.warn("could not resolve element name of parameter of {}: {}. Defined types were: {}", functionName, elementName, elementNameToElement.keySet());
//            return null;
//        }
//        final Type.Complex resolvedInput = typeNameToType.get(element.getDataType());
//        if (null == resolvedInput) {
//            log.warn("could not resolve parameter of {}: {}. Defined types were: {}", functionName, element, typeNameToType.keySet());
//        }
//        return resolvedInput;
//    }

}
