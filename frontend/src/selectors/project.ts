/**
 * This module defines `reselect` selectors for use in the containers.
 *
 * A `reselect` selector is a memoized function computing derived state from the
 * redux state. Correct memoization allows react to skip rendering a component if
 * the props shallowly equal, which improves performance for us.
 */

import { Dictionary } from 'lodash';
import values from 'lodash-es/values';
import {
    DomainItems,
    DomainItemType,
    Entity,
    Project,
    ProjectRevision,
} from 'omp-schema';
import { xsdRootElementSelector } from 'omp-schema/xsd';
import { createSelector } from 'reselect';

import { noItemBase } from "../actions/sync";
import { State } from '../state';

const routerParamsSelector = (s: State) => s.router.params || {};

export const commentsSelector = (s: State) => s.projectData.comments;
export const dataTypeDocumentationsSelector = (s: State) => s.projectData.dataTypeDocumentations;
export const dataTypesSelector = (s: State) => s.projectData.dataTypes;
export const elementsSelector = (s: State) => s.projectData.elements;
export const functionsSelector = (s: State) => s.projectData.functions;
export const projectsSelector = (s: State) => s.projects;
export const projectRevisionsSelector = (s: State) => s.projectRevisions;
export const sequencesSelector = (s: State) => s.projectData.sequences;
export const servicesSelector = (s: State) => s.projectData.services;
export const xsdSelector = (s: State) => s.projectData.xsd;
export const xsdAnalysisSelector = (s: State) => s.projectData.xsdAnalysis;

/**
 * Gets the ID and the type of the item that is currently being edited.
 *
 * @param {RouterData} routerData application router data
 * @returns {[string , DomainItemType]} a tuple consisting of the current domain item's ID and its type
 */
export const currentItemMetadataSelector: (s: State) => [string | null, DomainItemType | null] = createSelector(
    routerParamsSelector,
    (params: Dictionary<string>): [string | null, DomainItemType | null] => {
        if (params.datatypeId) {
            return [params.datatypeId, DomainItemType.DataType];
        } else if (params.functionId) {
            return [params.functionId, DomainItemType.Function];
        } else if (params.sequenceId) {
            return [params.sequenceId, DomainItemType.Sequence];
        } else if (params.serviceId) {
            return [params.serviceId, DomainItemType.Service];
        } else {
            return [noItemBase, null];
        }
    },
);

/**
 * Gets the current project.
 *
 * @param {State} state the app state
 * @returns {Project | null} the current project or null, if no project is open.
 */
export const currentProjectSelector = createSelector(
    routerParamsSelector,
    projectsSelector,
    ({ projectUrlSlug }, projects: Dictionary<Project>) => {
        return values(projects).find(p => p.urlSlug === projectUrlSlug) || null;
    },
);

/**
 * Gets the current project revision.
 *
 * @param {State} state the app state
 * @returns {ProjectRevision | null} the current project revision or null, if no project is open.
 */
export const currentProjectRevisionSelector = createSelector(
    routerParamsSelector,
    projectRevisionsSelector,
    currentProjectSelector,
    ({ revisionUrlSlug }, projectRevisions: Dictionary<ProjectRevision>, project: Project): ProjectRevision | null => {
        if (project && revisionUrlSlug) {
            if (revisionUrlSlug === "live") {
                return projectRevisions[project.liveRevision];
            }
            // find the correct revision. Iterate over revisions in state.
            return values(projectRevisions)
                .find(p => p.urlSlug === revisionUrlSlug && p.project === project.id) || null;
        }
        return null;
    },
);

/**
 * Gets the domain item that is currently being edited.
 *
 * @param {State} state the current app state
 * @returns {[DomainItems , DomainItemType]} a tuple consisting of the current domain item and its type
 */
export const currentItemSelector = createSelector(
    dataTypesSelector,
    dataTypeDocumentationsSelector,
    functionsSelector,
    sequencesSelector,
    servicesSelector,
    currentItemMetadataSelector,
    (dataTypes, dataTypeDocumentations, functions, sequences, services, [id, type]):
    [DomainItems | null, DomainItemType | null] => {
        if (!id || !type) {
            return [null, null];
        }

        switch (type as DomainItemType) {
            case DomainItemType.DataType:
                const dt = {
                    ...(dataTypes[id]),
                    ...(dataTypeDocumentations[id]),
                };
                return [dt, type];
            case DomainItemType.Function:
                return [functions[id], type];
            case DomainItemType.Sequence:
                return [sequences[id], type];
            case DomainItemType.Service:
                return [services[id], type];
            default:
                throw new Error("Unknown domain item type.");
        }
    },
);

/**
 * Gets the comments of the currently displayed item.
 *
 * @param {State} state the current app state
 * @returns {Comment[]} the comments belonging to the currently displayed item.
 */
export const currentItemCommentsSelector = createSelector(
    currentItemMetadataSelector,
    commentsSelector,
    ([id, ty], comments) => (id && ty) || (id === noItemBase)
        ? values(comments[id]).sort((a, b) => a.createdOn - b.createdOn)
        : [],
);

/**
 * Creates a selector that converts a record of OMP items into an array sorted
 * by item creation time.
 *
 * @param {Record<string, T extends OmpItem>} items the record of OMP items
 * @returns {T[]} the OMP items sorted by their creation date
 */
export const sortedListSelectorFactory = <T extends Entity>(selector: (s: State) => Dictionary<T>) => createSelector(
    selector,
    items => values(items).sort((a, b) => a.createdOn - b.createdOn),
);

/*
 * Applied sorted list selectors below.
 */

export const sortedDataTypesSelector = sortedListSelectorFactory(dataTypesSelector);
export const sortedElementsSelector = sortedListSelectorFactory(elementsSelector);
export const sortedFunctionsSelector = sortedListSelectorFactory(functionsSelector);
export const sortedProjectsSelector = sortedListSelectorFactory(projectsSelector);
export const sortedProjectRevisionsSelector = sortedListSelectorFactory(projectRevisionsSelector);
export const sortedSequencesSelector = sortedListSelectorFactory(sequencesSelector);
export const sortedServicesSelector = sortedListSelectorFactory(servicesSelector);

/**
 * Gets the root element of the current XSD document.
 */
export const currentXsdRootElementSelector = createSelector(
    xsdSelector,
    xsdRootElementSelector,
);
