import { Express, RequestHandler } from 'express';

import { API_BASE } from '../config';

import content from './content';
import project from "./project";
import snapshots from "./snapshots";
import user from './user';

/**
 * Converts request handlers that possibly return promises to something that is
 * compatible with expressjs' way of error handling.
 *
 * @param {RequestHandler} handler the method handler to execute.
 * @returns {RequestHandler} a new request handler that handles any promise errors that might occur.
 */
export function handlify(handler: RequestHandler): RequestHandler {
    return (req, res, next) => Promise.resolve(handler(req, res, next)).catch(next);
}

export default function(app: Express) {
    content(app, `${API_BASE}/content`);
    project(app, `${API_BASE}/project`);
    snapshots(app, `${API_BASE}/snapshots`);
    user(app, `${API_BASE}/user`);
}
