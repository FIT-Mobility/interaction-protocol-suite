import * as mongoose from 'mongoose';

export interface User extends mongoose.Document {
    avatar: string;
    createdAt: Date;
    email: string;
    name: string;
    passHash: string;

    safe(): Partial<User>;
}

const schema = new mongoose.Schema({
    avatar: String,
    createdAt: Date,
    email: {
        type: String,
        required: true,
        index: true,
        unique: true,
        /* tslint:disable-next-line:max-line-length */
        validate: /(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/,
    },
    name: {
        type: String,
        required: true,
    },
    passHash: {
        type: String,
        required: true,
    },
}).pre('save', function(this: User, next: () => any) {
    if (!this.createdAt) {
        this.createdAt = new Date();
    }
    next();
}).method('safe', function(this: User): Partial<User> {
    return {
        id: this._id,
        avatar: this.avatar,
        createdAt: this.createdAt,
        email: this.email,
        name: this.name,
    };
});

export default mongoose.model<User>('User', schema);
