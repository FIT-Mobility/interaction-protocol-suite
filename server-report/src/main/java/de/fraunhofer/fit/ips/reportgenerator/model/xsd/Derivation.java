package de.fraunhofer.fit.ips.reportgenerator.model.xsd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeDefinition;

import javax.xml.namespace.QName;
import java.util.function.Function;

import static org.apache.xerces.impl.xs.SchemaSymbols.ATTVAL_ANYTYPE;
import static org.apache.xerces.impl.xs.SchemaSymbols.URI_SCHEMAFORSCHEMA;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 * @since 23.02.2018
 */
@RequiredArgsConstructor
@ToString
@Getter
public class Derivation {

    private static Derivation NO_DERIVATION = new Derivation(Type.NONE, new QName(URI_SCHEMAFORSCHEMA, ATTVAL_ANYTYPE));

    private final Type type;
    private final QName baseName;

    public static Derivation newInstance(XSComplexTypeDecl complex, Function<XSTypeDefinition, QName> nameGetter) {
        Type type = Type.newInstance(complex);
        switch (type) {
            case NONE:
                return NO_DERIVATION;
            case EXTENSION:
            case RESTRICTION:
                return new Derivation(type, nameGetter.apply(complex.getBaseType()));
            default:
                throw new IllegalArgumentException("Could not decide Derivation of complexType");
        }
    }

    public enum Type {
        NONE,
        EXTENSION,
        RESTRICTION;

        public static Type newInstance(XSComplexTypeDecl complex) {
            short type = complex.getDerivationMethod();
            if (type == XSConstants.DERIVATION_EXTENSION) {
                return EXTENSION;
            } else if (type == XSConstants.DERIVATION_RESTRICTION) {
                if (isAnyType(complex.getBaseType())) {
                    return NONE;
                } else {
                    return RESTRICTION;
                }
            } else {
                throw new IllegalArgumentException("Could not decide Derivation.Type of complexType");
            }
        }

        private static boolean isAnyType(XSTypeDefinition baseType) {
            if (baseType == null) {
                // assume NONE; this is the case e.g. for XSD types
                return true;
            } else {
                return ATTVAL_ANYTYPE.equals(baseType.getName()) && URI_SCHEMAFORSCHEMA.equals(baseType.getNamespace());
            }
        }
    }

}
