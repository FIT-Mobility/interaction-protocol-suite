import * as bcrypt from 'bcrypt';
import { Express } from 'express';
import * as mongoose from 'mongoose';

import { BCRYPT_ROUNDS } from '../config';
import {
    ensureLoggedIn,
    ensureObjIdValid,
    ensurePresent,
    File,
    FileModel,
    User,
    UserModel,
} from '../model';

import { handlify } from '.';

export default function(app: Express, base: string) {
    app.get(`${base}/me`, handlify(async (req, res) => {
        if (!ensureLoggedIn(req, res)) {
            return;
        }

        const user = await UserModel.findById(req.session!.userId);
        if (!user) {
            return res.status(500).json({
                success: false,
                msg: "Logged in user not found. This is a bug - please report it to the administrator.",
            });
        }

        res.json({
            success: true,
            data: user.safe(),
        });
    }));

    app.get(`${base}/:userIdOrEmail`, handlify(async (req, res) => {
        const id = req.params.userIdOrEmail;
        const user = await (mongoose.Types.ObjectId.isValid(id)
            ? UserModel.findById(id)
            : UserModel.findOne({ email: id }));

        if (!user) {
            return res.status(404).json({
                success: false,
                msg: "User not found.",
            });
        }

        res.json({
            success: true,
            data: user.safe(),
        });
    }));

    app.post(base, handlify(async (req, res) => {
        if (!ensurePresent(req, res, 'email', 'name', 'password')) {
            return;
        }

        const passHash = await bcrypt.hash(req.body.password, BCRYPT_ROUNDS);
        const user = new UserModel({
            email: req.body.email,
            name: req.body.name,
            passHash,
        });

        try {
            await user.save();
        } catch (err) {
            if (err.name === 'ValidationError') {
                return res.status(400).json({
                    success: false,
                    data: err.message,
                    msg: "Validation failed.",
                });
            } else if (err.code === 11000) { // Duplicate key error -> duplicate E-Mail in our case
                return res.status(400).json({
                    success: false,
                    msg: "This E-Mail address is already taken. Please use a different one.",
                });
            } else {
                console.log(err);
                return res.status(500).json({
                    success: false,
                    msg: "Failed to save user to database.",
                });
            }
        }

        req.session!.userId = user._id;
        return res.json({
            success: true,
            data: user.safe(),
        });
    }));

    app.post(`${base}/login`, handlify(async (req, res) => {
        const user = await UserModel.findOne({ email: req.body.email });
        if (user && await bcrypt.compare(req.body.password, user.passHash)) {
            req.session!.userId = user._id;

            return res.status(200).json({
                success: true,
                data: user.safe(),
            });
        } else {
            return res.status(401).json({
                success: false,
                msg: "Either the user doesn't exist, or the password was incorrect.",
            });
        }
    }));

    app.post(`${base}/logout`, (req, res) => {
        req.session!.destroy(err => {
            if (!err) {
                res.json({ success: true });
            } else {
                res.status(500).json({
                    success: false,
                    msg: "Could not destroy session.",
                });
            }
        });
    });

    app.put(`${base}/profile`, handlify(async (req, res) => {
        if (!ensureLoggedIn(req, res) || !ensureObjIdValid(res, req.body.imageId)) {
            return;
        }

        const [user, image] = await Promise.all([
            UserModel.findById(req.session!.userId),
            FileModel.findById(req.body.imageId),
        ] as any);
        if (!user) {
            return res.status(404).json({
                success: false,
                msg: "Couldn't find user.",
            });
        }
        if (!image) {
            return res.status(404).json({
                success: false,
                msg: "Couldn't find image.",
            });
        }

        const { mime } = image as File;
        if (mime.indexOf('image/') === -1) {
            return res.status(400).json({
                success: false,
                msg: `Cannot set profile picture to something thats not an image. Mime type is ${mime}.`,
            });
        }

        try {
            await (user as User).update({ avatar: req.body.imageId });
        } catch (err) {
            console.log(err);
            res.status(500).json({
                success: false,
                msg: "Couldn't update user in DB.",
            });
        }

        res.status(200).json({ success: true });
    }));
}
