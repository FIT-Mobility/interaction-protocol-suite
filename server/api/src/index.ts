import * as bodyParser from 'body-parser';
import * as mongoSession from 'connect-mongo';
import * as cors from 'cors';
import * as express from 'express';
import * as session from 'express-session';
import * as http from 'http';
import * as morgan from 'morgan';

import {
    CORS_ORIGINS,
    MONGO_CONNECTION_TIMEOUT,
    MONGO_URL,
    SESSION_SECRET,
} from './config';
import routes from './routes';
import startY from './yjs';

// Usage of `require` necessary to be able to assign Promise impl
/* tslint:disable-next-line:no-var-requires */
const mongoose = require('mongoose');
mongoose.Promise = global.Promise;

const MongoStore = mongoSession(session);

async function main(): Promise<void> {
    await connect();

    const app = express();
    const server = new http.Server(app);
    const sess = session({
        resave: false,
        saveUninitialized: true,
        secret: SESSION_SECRET,
        store: new MongoStore({
            mongooseConnection: mongoose.connection,
        }),
    });

    app.use(morgan('⚡️  :method :url :status :res[content-length] - :response-time ms'));
    app.use(sess);

    console.log(`⚙️  Configuring CORS for ${CORS_ORIGINS.join(', ')}`);
    app.use(cors({
        credentials: true,
        exposedHeaders: 'Warning',
        origin: true, // CORS_ORIGINS,
    }));

    app.use(bodyParser.json());
    app.use(bodyParser.urlencoded({ extended: false }));

    routes(app);

    app.use((err, req, res, next) => {
        console.error("💥  An unhandled error has occured in one of the request handlers:");
        console.error(err);
        res.status(500).json({
            success: false,
            msg: "An unknown error occured.",
        });
    });

    startY(server, sess);
    server.listen(8080, () => console.log("💫  Server listening on port 8080"));
}

async function connect() {
    console.log("🗃  Connecting to MongoDB on", MONGO_URL);

    const start = Date.now();
    while (true) {
        try {
            await mongoose.connect(MONGO_URL, { useMongoClient: true });
            return;
        } catch (err) {
            if ((Date.now() - start) <= MONGO_CONNECTION_TIMEOUT) {
                if (err.message.indexOf('ECONNREFUSED') !== -1 || err.message.indexOf('ENOTFOUND') !== -1) {
                    console.log("☝️  MongoDB not online yet - waiting 2000ms and trying again");
                    await (new Promise(res => setTimeout(res, 2000)));
                } else {
                    throw err;
                }
            } else {
                console.log("⏱  MongoDB connection timed out");
                throw err;
            }
        }
    }
}

main()
    .catch(err => {
        console.error(err);
        console.error("💥  An unhandled error occured whithin the server's main function. Exiting...");

        process.exit(1);
    });
