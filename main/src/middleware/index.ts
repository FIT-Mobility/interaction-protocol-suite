import existingFilesMw from './existing-files';
import fetchUserMw from './fetch-users';
import generatePreviewMw from './preview-validate';
import routeYjsSyncMw from './route-yjs-sync';
import parseXsdDataTypesMw from './xsd-analysis';

export default [
    existingFilesMw as any,
    fetchUserMw as any,
    generatePreviewMw as any,
    parseXsdDataTypesMw as any,
    routeYjsSyncMw as any,
];
