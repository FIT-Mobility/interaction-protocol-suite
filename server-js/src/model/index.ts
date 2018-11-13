import { Request, Response } from 'express';
import * as mongoose from 'mongoose';
import * as multer from 'multer';

import FileModel, { File } from './file';
import UserModel, { User } from './user';

/**
 * Creates an upload handler for a single file.
 *
 * @param multipartName the name of the file in the multipart body.
 * @param dest the destination folder to upload to.
 */
export function createAcceptSingleFile(multipartName: string, dest: string) {
    const upload = multer({ dest }).single(multipartName);
    return (req: Request, resp: Response) => {
        return new Promise<void>((res, rej) => upload(req, resp, err => err ? rej(err) : res()));
    };
}

/**
 * Ensures the user is logged in and sends a 401 otherwise.
 *
 * @param {Request} req the request object
 * @param {Response} res the response
 * @returns {boolean} whether the user was logged in. If this is false, return from your endpoint handler.
 */
export function ensureLoggedIn(req: Request, res: Response): boolean {
    if (!req.session!.userId) {
        res.status(401).json({
            success: false,
            msg: "Not logged in.",
        });
        return false;
    }
    return true;
}

/**
 * Checks whether the given object IDs are valid and automatically sends a default
 * response, in case they're invalid.
 *
 * @param {Response} res the response object.
 * @param {string} ids the object IDs to check
 * @returns {boolean} whether all IDs were valid. If this is false, return from your endpoint handler.
 */
export function ensureObjIdValid(res: Response, ...ids: (string | number)[]): boolean {
    if (!ids.every(mongoose.Types.ObjectId.isValid)) {
        res.status(400).json({
            success: false,
            msg: "ID could not be parsed.",
        });
        return false;
    }

    return true;
}

/**
 * Ensures all the given fields are present in the request body
 * and sends a 400 to the client if a field is missing.
 *
 * @param {Request} req the request
 * @param {Response} resp the response
 * @param {string} fields the fields to check for
 * @returns {boolean} whether all fields were present. If this is false, return from your endpoint handler.
 */
export function ensurePresent(
    req: Request,
    resp: Response,
    ...fields: string[]): boolean {
    const missingFields = fields.filter(f => !req.body[f]);
    if (missingFields.length > 0) {
        resp.status(400).json({
            success: false,
            msg: `Missing fields '${missingFields.join(', ')}'.`,
        });
        return false;
    }

    return true;
}

export {
    File,
    FileModel,
    User,
    UserModel,
};
