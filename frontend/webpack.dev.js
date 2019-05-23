const base = require('./webpack');
const merge = require('webpack-merge');

const Dotenv = require('dotenv-webpack');

module.exports = merge(base, {
    devServer: {
        historyApiFallback: true
    },
    mode: 'development',
    devtool: 'source-map',
    plugins: [
        new Dotenv({
            path: './.env.dev',
            safe: true,
            defaults: true
        }),
    ],
});