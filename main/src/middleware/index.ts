import existingFilesMw from './existing-files';
import fetchUserMw from './fetch-users';
import generatePreviewMw from './preview-validate';
import readOnlyMw from './read-only';
import routeYjsSyncMw from './route-yjs-sync';
import parseXsdDataTypesMw from './xsd-analysis';

export default [
    readOnlyMw as any,
    existingFilesMw as any,
    fetchUserMw as any,
    generatePreviewMw as any,
    parseXsdDataTypesMw as any,
    routeYjsSyncMw as any,
];
