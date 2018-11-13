/**
 * The unique identifier of each XSD element type .
 */
export enum ElementTypes {
    XS_Schema = 'schema',
    XS_Include = 'include',
    XS_Import = 'import',
    XS_Redefine = 'redefine',
    // XS_Override = 'override',
    XS_Annotation = 'annotation',
    XS_SimpleType = 'simpleType',
    XS_ComplexType = 'complexType',
    XS_Group = 'group',
    XS_AttributeGroup = 'attributeGroup',
    XS_Element = 'element',
    XS_Attribute = 'attribute',
    XS_All = 'all',
    XS_Choice = 'choice',
    XS_Sequence = 'sequence',
    XS_SimpleContent = 'simpleContent',
    XS_ComplexContent = 'complexContent',
    XS_SimpleTypeRestriction = 'simpleTypeRestriction',
    XS_SimpleContentRestriction = 'simpleContentRestriction',
    XS_ComplexContentRestriction = 'complexContentRestriction',
    XS_SimpleContentExtension = 'simpleContentExtension',
    XS_ComplexContentExtension = 'complexContentExtension',
    XS_Any = 'any',
    XS_AnyAttribute = 'anyAttribute',
    XS_Unique = 'unique',
    XS_Key = 'key',
    XS_KeyRef = 'keyRef',
    XS_Selector = 'selector',
    XS_Field = 'field',
    XS_List = 'list',
    XS_Union = 'union',
    XS_Enumeration = 'enumeration',
    XS_MinExclusive = 'minExclusive',
    XS_MaxExclusive = 'maxExclusive',
    XS_MinInclusive = 'minInclusive',
    XS_MaxInclusive = 'maxInclusive',
    XS_TotalDigits = 'totalDigits',
    XS_FractionDigits = 'fractionDigits',
    XS_Length = 'length',
    XS_MinLength = 'minLength',
    XS_MaxLength = 'maxLength',
    XS_Pattern = 'pattern',
    XS_Documentation = 'documentation',
    XS_Assertion = 'assertion',
    XS_Assert = 'assert',
    // XS_OpenContent = 'openContent',
    XS_WhiteSpace = 'whiteSpace',
    // XS_ExplicitTimezone = 'explicitTimezone',
    // XS_Alternative = 'alternative',
    XS_Notation = 'notation',
    XS_AppInfo = 'appinfo',
    // XS_DefaultOpenContent = 'defaultOpenContent',
}

/**
 * The unique identifier of each XSD attribute type.
 */
export enum AttributeTypes {
    ID = 'ID',
    Block = 'BLOCK',
    Final = 'FINAL',
    TargetNameSpace = 'TARGETNAMESPACE',
    AttributeFormDefault = 'ATTRIBUTEFORMDEFAULT',
    ElementFormDefault = 'ELEMENTFORMDEFAULT',
    XML_Lang = 'XML_LANG',
    SchemaLocation = 'SCHEMALOCATION',
    Namespace = 'NAMESPACE',
    Name = 'NAME',
    Mixed = 'MIXED',
    Abstract = 'ABSTRACT',
    Type = 'TYPE',
    SubstitutionGroup = 'SUBSTITUTIONGROUP',
    Default = 'DEFAULT',
    Ref = 'REF',
    Fixed = 'FIXED',
    Use = 'USE',
    Form = 'FORM',
    MinOccurs = 'MINOCCURS',
    MaxOccurs = 'MAXOCCURS',
    Base = 'BASE',
    ProcessContents = 'PROCESSCONTENTS',
    Value = 'VALUE',
    MemberTypes = 'MEMBERTYPES',
    XPath = 'XPATH',
    Refer = 'REFER',
    ItemType = 'ITEMTYPE',
    Version = 'VERSION',
    Source = 'SOURCE',
    Test = 'TEST',
    XPathDefaultNamespace = 'XPATHDEFAULTNAMESPACE',
    // Mode = 'MODE',
    WhiteSpaceValue = 'WHITESPACEVALUE',
    // ExplicitTimezoneValue = 'EXPLICITTIMEZONEVALUE',
    // Inheritable = 'INHERITABLE',
    Nillable = 'NILLABLE',
    // DefaultAttributesApply = 'DEFAULTATTRIBUTESAPPLY',
    // NotNameSpace = 'NOTNAMESPACE',
    // NotQName = 'NOTQNAME',
    Public = 'PUBLIC',
    System = 'SYSTEM',
    BlockDefault = 'BLOCKDEFAULT',
    // DefaultAttributes = 'DEFAULTATTRIBUTES',
    FinalDefault = 'FINALDEFAULT',
    // AppliesToEmpty = 'APPLIESTOEMPTY',
}

/**
 * The type of the schema tree.
 */
export interface SchemaTree {
    attributes: {
        [k in AttributeTypes]: AttributeSchema;
    };
    elements: {
        [k in ElementTypes]: ElementSchema;
    };
}

export interface AttributeSchema {
    name: string;
    namespace: string;
    values: string[];
}

export interface ElementSchema {
    name: string;
    namespace: string;
    attributes: AttributeTypes[];
    children: ElementTypes[];
}

/**
 * model for creating XSD documents based on XMLSchema.xsd
 * separated into basic elements and attributes (all items are indexed by an arbitrary ID
 * elements contain references on possible attributes and child elements (+ namespace)
 *
 * @type elements, attributes
 */
const schemaTree: SchemaTree = {
    elements: {
        // definitions from and in order of https://www.w3.org/TR/xmlschema11-1/
        [ElementTypes.XS_Attribute]: {
            name: "attribute",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                AttributeTypes.Type,
                AttributeTypes.Ref,
                AttributeTypes.Default,
                AttributeTypes.ID,
                AttributeTypes.Fixed,
                AttributeTypes.Form,
                // AttributeTypes.TargetNameSpace,
                AttributeTypes.Use,
                // AttributeTypes.Inheritable,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_SimpleType,
            ],
        },
        [ElementTypes.XS_Element]: {
            name: "element",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                AttributeTypes.Ref,
                AttributeTypes.Type,
                AttributeTypes.MaxOccurs,
                AttributeTypes.MinOccurs,
                AttributeTypes.Abstract,
                AttributeTypes.Block,
                AttributeTypes.Default,
                AttributeTypes.Final,
                AttributeTypes.Fixed,
                AttributeTypes.Form,
                AttributeTypes.ID,
                AttributeTypes.Nillable,
                AttributeTypes.SubstitutionGroup,
                AttributeTypes.TargetNameSpace,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_SimpleType,
                ElementTypes.XS_ComplexType,
                // ElementTypes.XS_Alternative,
                ElementTypes.XS_Unique,
                ElementTypes.XS_Key,
                ElementTypes.XS_KeyRef,
            ],
        },
        [ElementTypes.XS_ComplexType]: {
            name: "complexType",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                AttributeTypes.Abstract,
                AttributeTypes.Block,
                AttributeTypes.Final,
                AttributeTypes.ID,
                AttributeTypes.Mixed,
                // AttributeTypes.DefaultAttributesApply,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Sequence,
                ElementTypes.XS_Choice,
                ElementTypes.XS_Assert,
                ElementTypes.XS_All,
                ElementTypes.XS_SimpleContent,
                ElementTypes.XS_ComplexContent,
                // ElementTypes.XS_OpenContent,
                ElementTypes.XS_Group,
                ElementTypes.XS_Attribute,
                ElementTypes.XS_AttributeGroup,
                ElementTypes.XS_AnyAttribute,
            ],
        },
        [ElementTypes.XS_SimpleContent]: {
            name: "simpleContent",
            namespace: "xs",
            attributes: [
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_SimpleContentRestriction,
                ElementTypes.XS_SimpleContentExtension,
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_SimpleContentRestriction]: {
            name: "restriction",
            namespace: "xs",
            attributes: [
                AttributeTypes.Base,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Assertion,
                ElementTypes.XS_MinExclusive,
                ElementTypes.XS_MinInclusive,
                ElementTypes.XS_MaxExclusive,
                ElementTypes.XS_MaxInclusive,
                ElementTypes.XS_TotalDigits,
                ElementTypes.XS_FractionDigits,
                ElementTypes.XS_Length,
                ElementTypes.XS_MinLength,
                ElementTypes.XS_MaxLength,
                ElementTypes.XS_Enumeration,
                ElementTypes.XS_WhiteSpace,
                ElementTypes.XS_Pattern,
                ElementTypes.XS_Attribute,
                ElementTypes.XS_AttributeGroup,
                ElementTypes.XS_Annotation,
                ElementTypes.XS_SimpleType,
                ElementTypes.XS_AnyAttribute,
                ElementTypes.XS_Assert,
            ],
        },
        [ElementTypes.XS_SimpleContentExtension]: {
            name: "extension",
            namespace: "xs",
            attributes: [
                AttributeTypes.Base,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Assert,
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Attribute,
                ElementTypes.XS_AttributeGroup,
                ElementTypes.XS_AnyAttribute,
            ],
        },
        [ElementTypes.XS_ComplexContent]: {
            name: "complexContent",
            namespace: "xs",
            attributes: [
                AttributeTypes.ID,
                AttributeTypes.Mixed,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_ComplexContentRestriction,
                ElementTypes.XS_ComplexContentExtension,
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_ComplexContentRestriction]: {
            name: "restriction",
            namespace: "xs",
            attributes: [
                AttributeTypes.Base,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Sequence,
                ElementTypes.XS_Choice,
                ElementTypes.XS_Assert,
                ElementTypes.XS_Annotation,
                // ElementTypes.XS_OpenContent,
                ElementTypes.XS_Group,
                ElementTypes.XS_All,
                ElementTypes.XS_Attribute,
                ElementTypes.XS_AttributeGroup,
                ElementTypes.XS_AnyAttribute,
            ],
        },
        [ElementTypes.XS_ComplexContentExtension]: {
            name: "extension",
            namespace: "xs",
            attributes: [
                AttributeTypes.Base,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Sequence,
                ElementTypes.XS_Choice,
                ElementTypes.XS_Assert,
                ElementTypes.XS_Annotation,
                // ElementTypes.XS_OpenContent,
                ElementTypes.XS_Group,
                ElementTypes.XS_All,
                ElementTypes.XS_Attribute,
                ElementTypes.XS_AttributeGroup,
                ElementTypes.XS_AnyAttribute,
            ],
        },
        // [ElementTypes.XS_OpenContent]: {
        //     name: "openContent",
        //     namespace: "xs",
        //     attributes: [
        //         AttributeTypes.Mode,
        //         AttributeTypes.ID,
        //         // any attributes with non-schema namespace
        //     ],
        //     children: [
        //         ElementTypes.XS_Any,
        //         ElementTypes.XS_Annotation,
        //     ],
        // },
        [ElementTypes.XS_AttributeGroup]: {
            name: "attributeGroup",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                AttributeTypes.Ref,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Attribute,
                ElementTypes.XS_AttributeGroup,
                ElementTypes.XS_AnyAttribute,
            ],
        },
        [ElementTypes.XS_Group]: {
            name: "group",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                AttributeTypes.Ref,
                AttributeTypes.MaxOccurs,
                AttributeTypes.MinOccurs,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Sequence,
                ElementTypes.XS_Choice,
                ElementTypes.XS_All,
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_All]: {
            name: "all",
            namespace: "xs",
            attributes: [
                AttributeTypes.MaxOccurs,
                AttributeTypes.MinOccurs,
                AttributeTypes.ID,
                // any attribute with non-schema namespace
            ],
            children: [
                // ElementTypes.XS_Group,
                ElementTypes.XS_Element,
                // ElementTypes.XS_Any,
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Choice]: {
            name: "choice",
            namespace: "xs",
            attributes: [
                AttributeTypes.MaxOccurs,
                AttributeTypes.MinOccurs,
                AttributeTypes.ID,
                // any attribute with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Element,
                ElementTypes.XS_Group,
                ElementTypes.XS_Sequence,
                ElementTypes.XS_Choice,
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Any,
            ],
        },
        [ElementTypes.XS_Sequence]: {
            name: "sequence",
            namespace: "xs",
            attributes: [
                AttributeTypes.MaxOccurs,
                AttributeTypes.MinOccurs,
                AttributeTypes.ID,
                // any attribute with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Element,
                ElementTypes.XS_Group,
                ElementTypes.XS_Sequence,
                ElementTypes.XS_Choice,
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Any,
            ],
        },
        [ElementTypes.XS_Any]: {
            name: "any",
            namespace: "xs",
            attributes: [
                AttributeTypes.MaxOccurs,
                AttributeTypes.MinOccurs,
                AttributeTypes.Namespace,
                AttributeTypes.ProcessContents,
                // AttributeTypes.NotNameSpace,
                // AttributeTypes.NotQName,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_AnyAttribute]: {
            name: "any",
            namespace: "xs",
            attributes: [
                AttributeTypes.Namespace,
                AttributeTypes.ProcessContents,
                // AttributeTypes.NotNameSpace,
                // AttributeTypes.NotQName,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Unique]: {
            name: "unique",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                // AttributeTypes.Ref,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Selector,
                ElementTypes.XS_Field,
            ],
        },
        [ElementTypes.XS_Key]: {
            name: "key",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                // AttributeTypes.Ref,
                AttributeTypes.ID,
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Selector,
                ElementTypes.XS_Field,
                // any attributes with non-schema namespace
            ],
        },
        [ElementTypes.XS_KeyRef]: {
            name: "keyRef",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                // AttributeTypes.Ref,
                AttributeTypes.Refer,
                AttributeTypes.ID,
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_Selector,
                ElementTypes.XS_Field,
                // any attributes with non-schema namespace
            ],
        },
        [ElementTypes.XS_Selector]: {
            name: "selector",
            namespace: "xs",
            attributes: [
                AttributeTypes.XPath,
                // AttributeTypes.XPathDefaultNamespace,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Field]: {
            name: "field",
            namespace: "xs",
            attributes: [
                AttributeTypes.XPath,
                // AttributeTypes.XPathDefaultNamespace,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        // [ElementTypes.XS_Alternative]: {
        //     name: "alternative",
        //     namespace: "xs",
        //     attributes: [
        //         AttributeTypes.Test,
        //         AttributeTypes.Type,
        //         AttributeTypes.XPathDefaultNamespace,
        //         AttributeTypes.ID,
        //         // any attributes with non-schema namespace
        //     ],
        //     children: [
        //         ElementTypes.XS_Annotation,
        //         ElementTypes.XS_SimpleType,
        //         ElementTypes.XS_ComplexType,
        //     ],
        // },
        [ElementTypes.XS_Assert]: {
            name: "assert",
            namespace: "xs",
            attributes: [
                AttributeTypes.Test,
                AttributeTypes.XPathDefaultNamespace,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Notation]: {
            name: "notation",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                AttributeTypes.Public,
                AttributeTypes.System,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Annotation]: {
            name: "annotation",
            namespace: "xs",
            attributes: [
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Documentation,
                ElementTypes.XS_AppInfo,
            ],
        },
        [ElementTypes.XS_AppInfo]: {
            name: "appinfo",
            namespace: "xs",
            attributes: [
                AttributeTypes.Source,
            ],
            children: [
                // ({any})*
            ],
        },
        [ElementTypes.XS_Documentation]: {
            name: "documentation",
            namespace: "xs",
            attributes: [
                AttributeTypes.XML_Lang,
                AttributeTypes.Source,
            ],
            children: [
                // ({any})*
            ],
        },
        [ElementTypes.XS_SimpleType]: {
            name: "simpleType",
            namespace: "xs",
            attributes: [
                AttributeTypes.Name,
                AttributeTypes.Final,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_SimpleTypeRestriction,
                ElementTypes.XS_List,
                ElementTypes.XS_Union,
            ],
        },
        [ElementTypes.XS_SimpleTypeRestriction]: {
            name: "restriction",
            namespace: "xs",
            attributes: [
                AttributeTypes.Base,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Assertion,
                ElementTypes.XS_MinExclusive,
                ElementTypes.XS_MinInclusive,
                ElementTypes.XS_MaxExclusive,
                ElementTypes.XS_MaxInclusive,
                ElementTypes.XS_TotalDigits,
                ElementTypes.XS_FractionDigits,
                ElementTypes.XS_Length,
                ElementTypes.XS_MinLength,
                ElementTypes.XS_MaxLength,
                ElementTypes.XS_Enumeration,
                ElementTypes.XS_WhiteSpace,
                ElementTypes.XS_Pattern,
                // ElementTypes.XS_ExplicitTimezone,
                ElementTypes.XS_SimpleType,
                ElementTypes.XS_Annotation,
                // any with namespace: ##other
            ],
        },
        [ElementTypes.XS_List]: {
            name: "list",
            namespace: "xs",
            attributes: [
                AttributeTypes.ItemType,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_SimpleType,
            ],
        },
        [ElementTypes.XS_Union]: {
            name: "union",
            namespace: "xs",
            attributes: [
                AttributeTypes.MemberTypes,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_SimpleType,
            ],
        },
        [ElementTypes.XS_Schema]: {
            name: "schema",
            namespace: "xs",
            attributes: [
                AttributeTypes.TargetNameSpace,
                AttributeTypes.ElementFormDefault,
                AttributeTypes.AttributeFormDefault,
                AttributeTypes.XPathDefaultNamespace,
                AttributeTypes.BlockDefault,
                // AttributeTypes.DefaultAttributes,
                AttributeTypes.FinalDefault,
                AttributeTypes.ID,
                AttributeTypes.Version,
                AttributeTypes.XML_Lang,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_ComplexType,
                ElementTypes.XS_SimpleType,
                ElementTypes.XS_Element,
                ElementTypes.XS_Group,
                ElementTypes.XS_AttributeGroup,
                ElementTypes.XS_Attribute,
                ElementTypes.XS_Import,
                ElementTypes.XS_Include,
                ElementTypes.XS_Redefine,
                // ElementTypes.XS_Override,
                ElementTypes.XS_Annotation,
                // ElementTypes.XS_DefaultOpenContent,
                ElementTypes.XS_Notation,
            ],
        },
        // [ElementTypes.XS_DefaultOpenContent]: {
        //     name: "defaultOpenContent",
        //     namespace: "xs",
        //     attributes: [
        //         AttributeTypes.Mode,
        //         AttributeTypes.AppliesToEmpty,
        //         AttributeTypes.ID,
        //         // any attributes with non-schema namespace
        //     ],
        //     children: [
        //         ElementTypes.XS_Annotation,
        //         ElementTypes.XS_Any,
        //     ],
        // },
        [ElementTypes.XS_Include]: {
            name: "include",
            namespace: "xs",
            attributes: [
                AttributeTypes.SchemaLocation,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Redefine]: {
            name: "redefine",
            namespace: "xs",
            attributes: [
                AttributeTypes.SchemaLocation,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
                ElementTypes.XS_SimpleType,
                ElementTypes.XS_ComplexType,
                ElementTypes.XS_Group,
                ElementTypes.XS_AttributeGroup,
            ],
        },
        // [ElementTypes.XS_Override]: {
        //     name: "override",
        //     namespace: "xs",
        //     attributes: [
        //         AttributeTypes.SchemaLocation,
        //         AttributeTypes.ID,
        //         // any attributes with non-schema namespace
        //     ],
        //     children: [
        //         ElementTypes.XS_Annotation,
        //         ElementTypes.XS_SimpleType,
        //         ElementTypes.XS_ComplexType,
        //         ElementTypes.XS_Group,
        //         ElementTypes.XS_AttributeGroup,
        //         ElementTypes.XS_Element,
        //         ElementTypes.XS_Attribute,
        //         ElementTypes.XS_Notation,
        //     ],
        // },
        [ElementTypes.XS_Import]: {
            name: "import",
            namespace: "xs",
            attributes: [
                AttributeTypes.Namespace,
                AttributeTypes.SchemaLocation,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        // additional definitions from and in order of https://www.w3.org/TR/xmlschema11-2/
        [ElementTypes.XS_Length]: {
            name: "length",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_MinLength]: {
            name: "minLength",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_MaxLength]: {
            name: "maxLength",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Pattern]: {
            name: "pattern",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Enumeration]: {
            name: "enumeration",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_WhiteSpace]: {
            name: "whiteSpace",
            namespace: "xs",
            attributes: [
                AttributeTypes.WhiteSpaceValue,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_MaxInclusive]: {
            name: "maxInclusive",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_MaxExclusive]: {
            name: "maxExclusive",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_MinExclusive]: {
            name: "minExclusive",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_MinInclusive]: {
            name: "minInclusive",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_TotalDigits]: {
            name: "totalDigits",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_FractionDigits]: {
            name: "fractionDigits",
            namespace: "xs",
            attributes: [
                AttributeTypes.Value,
                AttributeTypes.Fixed,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        [ElementTypes.XS_Assertion]: {
            name: "assertion",
            namespace: "xs",
            attributes: [
                AttributeTypes.Test,
                AttributeTypes.XPathDefaultNamespace,
                AttributeTypes.ID,
                // any attributes with non-schema namespace
            ],
            children: [
                ElementTypes.XS_Annotation,
            ],
        },
        // [ElementTypes.XS_ExplicitTimezone]: {
        //     name: "explicitTimezone",
        //     namespace: "xs",
        //     attributes: [
        //         AttributeTypes.ExplicitTimezoneValue,
        //         AttributeTypes.Fixed,
        //         AttributeTypes.ID,
        //         // any attributes with non-schema namespace
        //     ],
        //     children: [
        //         ElementTypes.XS_Annotation,
        //     ],
        // },
    },

    attributes: {
        [AttributeTypes.ID]: {
            name: "id",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Block]: {
            name: "block",
            namespace: "",
            values: [], // (#all | Liste von (extension | restriction | substitution))
        },
        [AttributeTypes.Final]: {
            name: "final",
            namespace: "",
            values: [], // (#all | Liste von (extension | restriction))
        },
        [AttributeTypes.TargetNameSpace]: {
            name: "targetNamespace",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Version]: {
            name: "version",
            namespace: "",
            values: [],
        },
        [AttributeTypes.AttributeFormDefault]: {
            name: "attributeFormDefault",
            namespace: "",
            values: [], // (unqualified | qualified)
        },
        [AttributeTypes.ElementFormDefault]: {
            name: "elementFormDefault",
            namespace: "",
            values: [], // (unqualified | qualified)
        },
        [AttributeTypes.XML_Lang]: {
            name: "xml:lang",
            namespace: "",
            values: [],
        },
        [AttributeTypes.SchemaLocation]: {
            name: "schemaLocation",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Namespace]: {
            name: "namespace",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Name]: {
            name: "name",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Mixed]: {
            name: "mixed",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Abstract]: {
            name: "abstract",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Type]: {
            name: "type",
            namespace: "",
            values: [],
        },
        [AttributeTypes.SubstitutionGroup]: {
            name: "substitutionGroup",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Default]: {
            name: "default",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Fixed]: {
            name: "fixed",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Ref]: {
            name: "ref",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Use]: {
            name: "use",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Form]: {
            name: "form",
            namespace: "",
            values: [],
        },
        [AttributeTypes.MinOccurs]: {
            name: "minOccurs",
            namespace: "",
            values: [],
        },
        [AttributeTypes.MaxOccurs]: {
            name: "maxOccurs",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Base]: {
            name: "base",
            namespace: "",
            values: [],
        },
        [AttributeTypes.ProcessContents]: {
            name: "processContents",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Refer]: {
            name: "refer",
            namespace: "",
            values: [],
        },
        [AttributeTypes.XPath]: {
            name: "xpath",
            namespace: "",
            values: [],
        },
        [AttributeTypes.ItemType]: {
            name: "itemType",
            namespace: "",
            values: [],
        },
        [AttributeTypes.MemberTypes]: {
            name: "memberTypes",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Value]: {
            name: "value",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Source]: {
            name: "source",
            namespace: "",
            values: [],
        },
        [AttributeTypes.Test]: {
            name: "test",
            namespace: "",
            values: [],
        },
        [AttributeTypes.XPathDefaultNamespace]: {
            name: "xpathDefaultNamespace",
            namespace: "",
            values: [], // (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
        },
        // [AttributeTypes.Mode]: {
        //     name: "mode",
        //     namespace: "",
        //     values: [], // (none | interleave | suffix)
        // },
        [AttributeTypes.WhiteSpaceValue]: {
            name: "value",
            namespace: "",
            values: [], // (collapse | preserve | replace)
        },
        // [AttributeTypes.ExplicitTimezoneValue]: {
        //     name: "value",
        //     namespace: "",
        //     values: [],
        // },
        // [AttributeTypes.Inheritable]: {
        //     name: "inheritable",
        //     namespace: "",
        //     values: [],
        // },
        [AttributeTypes.Nillable]: {
            name: "nillable",
            namespace: "",
            values: [],
        },
        // [AttributeTypes.DefaultAttributesApply]: {
        //     name: "defaultAttributesApply",
        //     namespace: "",
        //     values: [],
        // },
        // [AttributeTypes.NotNameSpace]: {
        //     name: "notNameSpace",
        //     namespace: "",
        //     values: [],
        // },
        // [AttributeTypes.NotQName]: {
        //     name: "notQName",
        //     namespace: "",
        //     values: [],
        // },
        [AttributeTypes.Public]: {
            name: "public",
            namespace: "",
            values: [],
        },
        [AttributeTypes.System]: {
            name: "system",
            namespace: "",
            values: [],
        },
        [AttributeTypes.BlockDefault]: {
            name: "blockDefault",
            namespace: "",
            values: [],
        },
        // [AttributeTypes.DefaultAttributes]: {
        //     name: "defaultAttributes",
        //     namespace: "",
        //     values: [],
        // },
        [AttributeTypes.FinalDefault]: {
            name: "finalDefault",
            namespace: "",
            values: [],
        },
        // [AttributeTypes.AppliesToEmpty]: {
        //     name: "appliesToEmpty",
        //     namespace: "",
        //     values: [],
        // },
    },
};

export default schemaTree;
