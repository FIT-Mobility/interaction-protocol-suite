const base = require('./webpack');
const merge = require('webpack-merge');
const webpack = require('webpack');

const Dotenv = require('dotenv-webpack');
const Minify = require('terser-webpack-plugin');

module.exports = merge(base, {
    devtool: 'source-map',
    mode: 'none',
    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                'NODE_ENV': JSON.stringify('production')
            }
        }),
        new Dotenv({
            path: './.env.prod',
            safe: true,
            systemvars: true
        }),
        new Minify({
            cache: true,
            parallel: true,
            sourceMap: true,
            terserOptions: {
                ecma: 6,
                mangle: true,
                ie8: false,
                safari10: true,
            },
        }),
    ],
});