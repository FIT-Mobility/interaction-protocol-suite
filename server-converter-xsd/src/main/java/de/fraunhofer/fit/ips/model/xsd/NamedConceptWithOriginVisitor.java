package de.fraunhofer.fit.ips.model.xsd;

import de.fraunhofer.fit.ips.Visitor;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
public interface NamedConceptWithOriginVisitor extends Visitor {
    void visit(Element element);

    void visit(Type.Group group);

    void visit(Type.Complex complex);

    void visit(Type.Simple.Restriction restriction);

    void visit(Type.Simple.Enumeration enumeration);

    void visit(Type.Simple.List list);

    void visit(Type.Simple.Union union);

    void visit(Attributes.GlobalAttributeGroupDeclaration globalAttributeGroupDeclaration);

    void visit(Attributes.GlobalAttributeDeclaration globalAttributeDeclaration);
}
