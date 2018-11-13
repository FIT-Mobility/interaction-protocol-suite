import { load } from 'dotenv-safe';

load();

export const API_BASE = '/api';
export const BCRYPT_ROUNDS = parseInt(process.env.BCRYPT_ROUNDS!);
export const CORS_ORIGINS = process.env.CORS_ORIGINS!.split(',');
export const MONGO_CONNECTION_TIMEOUT = 30000;
export const MONGO_URL = process.env.MONGO_URL!;
export const SESSION_SECRET = process.env.SESSION_SECRET!;
export const UPLOAD_PATH = './uploads';
