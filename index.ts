import { ElementTypes } from './schema-tree';

export const PROJECT_ROOM = '__OMP_PROJECTS_v2__';

export const yjsShareGlobalProjects = {
    projects: 'Map',
    projectRevisions: 'Map',
};

export const yjsShareProjectRevision = {
    comments: 'Map',
    dataTypeDocumentations: 'Map',
    docTexts: 'Map',
    functions: 'Map',
    sequences: 'Map',
    services: 'Map',
    xsd: 'Array',
};

/* General Types */

export interface ArrayDelta<T> {
    index: number;
    length: number;
    values: T[];
}

/**
 * The documentation language.
 */
export const enum Language {
    German = 'de',
    English = 'en',
}

/**
 * Represents a value with an ID.
 */
export interface IdAble {
    /** The item's unique ID. */
    id: string;
}

/**
 * Represents an item with a creation date and an ID.
 */
export interface Entity extends IdAble {
    /** The date (in milliseconds from UTC) when the item was created. */
    createdOn: number;
}

/**
 * Represents an item that can be documented.
 */
export interface Documentable {
    /** The ID of the associated documentation text. */
    docText: Record<Language, string>;
}

/**
 * A comment on an item.
 */
export interface Comment extends Entity {
    /** The ID of the user that created the comment. */
    createdBy: string;

    /** The ID of the item the comment is placed on. */
    itemId: string;

    /** The comment HTML. */
    text: string;
}

/**
 * A data type.
 */
export interface DataType extends Entity, Namable {
    /** Whether the data type is represented by an XSD simple or complex type. */
    isComplex: boolean;
}

/**
 * An XSD element.
 */
export interface Element extends Entity, Namable {
    /** Whether the XSD element sits directly below the <schema> node. */
    isTopLevel: boolean;

    /** The data type definition this element refers to. */
    type: string;
}

/**
 * The part of the data of the data type that is stored within yjs.
 */
export interface DataTypeDocumentation extends IdAble, Documentable {
}

export interface Archivable {
    isArchived: boolean;
}

export interface Namable {
    name: string;
}

/**
 * The basic OMP item with an ID, a name, a documentation record, and its creation date.
 */
export interface OmpItem extends Entity, Documentable, Archivable, Namable {
}

/**
 * An abstract OMP function.
 */
export interface Function extends OmpItem {
    /** A list of XSD assertions. */
    assertions: string[];

    /** The ID of the request element. */
    request: string;

    /** The ID of the response element. */
    response: string;
}

/**
 * An abstract OMP sequence.
 */
export interface Sequence extends OmpItem {
    /** The sequence of functions (respectively their IDs) to execute (in order). */
    functions: string[];
}

/**
 * An abstract OMP service.
 */
export interface Service extends OmpItem {
    /** The functions (resp. their IDs) that can be executed. */
    functions: string[];

    /** The sequences (resp. their IDs) that can be executed. */
    sequences: string[];
}

/**
 * An XSD attribute definition.
 */
export interface XsdAttribute extends Entity, Namable {
    /** The attribute's position within the attribute list. */
    position: number;

    /** The attributes value. */
    value: string;
}

/**
 * A normalized XSD element definition.
 */
export interface XsdElement extends Entity, Namable {
    /** The elements attributes. */
    attributes: Record<string, XsdAttribute>;

    /**
     * The elements content.
     *
     * Either a string, if the element contains text, or a list of IDs of
     * the children elements.
     */
    content: null | string | string[];

    /** The ID of the parent element, or null, if the element sits at root level. */
    parentId: string | null;

    /** The element type of the element. */
    type: ElementTypes;
}

/**
 * All domain items as union type.
 */
export type DomainItems = (DataType | Function | Sequence | Service) & Partial<Documentable>;

/**
 * The type of a domain item.
 */
export const enum DomainItemType {
    DataType = 'datatype',
    Function = 'function',
    Sequence = 'sequence',
    Service = 'service',
}

/**
 * An OMP project.
 */
export interface Project extends Entity, Archivable, Namable {
    /** ID of the current live revision. */
    liveRevision: string;

    /** Human-readable version of the revision's name, lower-case, without spaces. May not be empty. */
    urlSlug: string;

    /** List of IDs of all revisions of this project. Also includes the live revision. */
    revisions: string[];
}

export const SERVER_BACKUP_USER = "-1337"; // :-)

/**
 * A specific revision of an OMP project.
 */
export interface ProjectRevision extends OmpItem {
    /** The base URI for relative XSD imports. */
    baseUri: string | null;

    /** Other projects this project is importing. */
    imports?: string[];

    /** The backend-file ID of the archive the files that can be imported where extracted from. */
    importableFilesArchiveId: string | null;

    /** The list of files that can be imported from the XSD editor. */
    importableFilesList: string[]

    /**
     * Human-readable version of the revision's name, lower-case, without spaces. May be empty if this is the live
     * revision.
     */
    urlSlug: string;

    /** ID of the user who created the revision, or SERVER_BACKUP_USER. */
    creatorId: string;

    /** Indicates whether this revision is read-only, that is, an immutable snapshot. */
    readOnly: boolean;

    /** ID of the project to which this revision belongs. */
    project: string;
}

/**
 * Represents a user.
 */
export interface User {
    /** The users ID. */
    id: string;

    /** The file ID of the users avatar. */
    avatar: string;

    /** The date the user was created on. */
    createdAt: Date;

    /** The users E-Mail. */
    email: string;

    /** The users name. */
    name: string;
}

/* XSD Types */

/**
 * The type of operation a user can perform on an XSD document.
 */
export const enum XsdOperationType {
    AddAttribute = 'ADD_ATTRIBUTE',
    AddNode = 'ADD_NODE',
    EditAttribute = 'EDIT_ATTRIBUTE',
    EditTextContent = 'EDIT_TEXT_CONTENT',
    MoveNode = 'MOVE_NODE',
    RemoveNode = 'REMOVE_NODE',
    RemoveAttribute = 'REMOVE_ATTRIBUTE',
}

/**
 * The XSD operation description for adding an attribute to an element.
 */
export interface AddAttributeXsdOperation {
    /** The operation type. */
    type: XsdOperationType.AddAttribute;

    /** The new internal ID of the attribute. */
    attributeId: string;

    /** The date (from UTC, in milliseconds) the attribute was created on. */
    createdOn: number;

    /** The ID of the element to add the attribute to. */
    elementId: string;

    /** The attributes name including the namespace. */
    name: string;

    /** The new position inside the attribute list. */
    position: number;

    /** The attributes value. */
    value: string;
}

/**
 * The XSD operation description for adding an element to an XSD document.
 */
export interface AddNodeXsdOperation {
    /** The operation type. */
    type: XsdOperationType.AddNode;

    /** The date (in milliseconds from UTC) when the element was created. */
    createdOn: number;

    /** The internal ID to assign to the element. */
    elementId: string;

    /** The element type of the element to be created. */
    elementType: ElementTypes;

    /**
     * The type of XSD element added including the namespace.
     *
     * @example xs:schema, xs:complexType, etc.
     */
    name: string;

    /**
     * The ID of the parent element.
     *
     * If this is null, the new element is at root level. It is an error to have two
     * elements at root level at the same time.
     */
    parentId: string | null;

    /**
     * The new position within the list of childs of the parent.
     *
     * Must be 0 if parentId is null.
     */
    position: number;
}

/**
 * The XSD operation description for editing the contents of an attribute.
 */
export interface EditAttributeXsdOperation {
    /** The operation type. */
    type: XsdOperationType.EditAttribute;

    /** The ID of the attribute to edit. */
    attributeId: string;

    /** The ID of the element to edit the attribute on. */
    elementId: string;
}

/**
 * The XSD operation description for editing the name of an attribute.
 */
export interface EditAttributeNameXsdOperation extends EditAttributeXsdOperation {
    /** The attributes new name including the namespace. */
    name: string;
}

/**
 * The XSD operation description for editing the value of an attribute.
 */
export interface EditAttributeValueXsdOperation extends EditAttributeXsdOperation {
    /** The attributes new value. */
    value: string;
}

/**
 * The XSD operation description for editing the text contents of an element.
 */
export interface EditTextContentXsdOperation {
    /** The operation type. */
    type: XsdOperationType.EditTextContent;

    /** The ID of the element being edited. */
    elementId: string;

    /** The new content of the element. */
    newContent: string;
}

/**
 * The XSD operation description for moving a node within its parent.
 */
export interface MoveNodeXsdOperation {
    /** The operation type. */
    type: XsdOperationType.MoveNode;

    /** The ID of element being moved. */
    elementId: string;

    /** The element's new position. */
    newPosition: number;
}

/**
 * The XSD operation description for removing an attribute from an element.
 */
export interface RemoveAttributeXsdOperation {
    /** The operation type. */
    type: XsdOperationType.RemoveAttribute;

    /** The ID of the element to remove the attribute from. */
    elementId: string;

    /** The attributes ID. */
    attributeId: string;
}

/**
 * The XSD operation description for removing a node from an XSD document.
 */
export interface RemoveNodeXsdOperation {
    /** The operation type. */
    type: XsdOperationType.RemoveNode;

    /** The ID of the node to remove. */
    elementId: string;
}

/**
 * All XSD operations as intersection type.
 */
export type XsdOperation =
    | AddAttributeXsdOperation
    | AddNodeXsdOperation
    | EditAttributeNameXsdOperation
    | EditAttributeValueXsdOperation
    | EditTextContentXsdOperation
    | MoveNodeXsdOperation
    | RemoveAttributeXsdOperation
    | RemoveNodeXsdOperation;

export const values = <T>(obj: Record<string, T>) => Object.keys(obj).map(k => obj[k]);