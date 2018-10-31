import * as mongoose from 'mongoose';
import * as path from 'path';

import { UPLOAD_PATH } from '../config';

export interface File extends mongoose.Document {
    createdAt: Date;
    filename: string;
    mime: string;

    getFullPath(): string;
    safe(): Partial<File>;
}

const schema = new mongoose.Schema({
    createdAt: Date,
    filename: {
        type: String,
        required: true,
        minlength: 1,
    },
    mime: {
        type: String,
        required: true,
    },
}).pre('save', function(this: File, next: () => any) {
    if (!this.createdAt) {
        this.createdAt = new Date();
    }
    next();
}).method('getFullPath', function(this: File) {
    return path.join(
        process.cwd(),
        UPLOAD_PATH,
        this.filename,
    );
}).method('safe', function(this: File): Partial<File> {
    return {
        id: this._id,
        createdAt: this.createdAt,
        mime: this.mime,
    };
});

export default mongoose.model<File>('File', schema);
