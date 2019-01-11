const CopyPlugin = require('copy-webpack-plugin');
const FaviconPlugin = require('webapp-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');
const webpack = require('webpack');

module.exports = {
    entry: './src/index.tsx',
    module: {
        rules: [{
            test: /\.(tsx|ts)/,
            exclude: /node_modules/,
            loader: 'tslint-loader',
            enforce: 'pre',
            options: {
                failOnHint: true
            }
        }, {
            test: /\.(tsx|ts)/,
            exclude: /node_modules/,
            use: 'awesome-typescript-loader'
        }, {
            test: /\.html/,
            exclude: /node_modules/,
            use: 'html-loader'
        }]
    },
    resolve: {
        extensions: ['.css', '.js', '.ts', '.tsx']
    },
    output: {
        publicPath: '/',
        path: path.resolve(__dirname, 'build'),
        filename: 'index.min.js'
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Interaction Protocol Specification and Validation Suite',
            template: 'src/index.html'
        }),
        new FaviconPlugin('./src/icon.png'),
        new CopyPlugin([{
            from: '../node_modules/quill/dist/*.css'
        }, {
            from: '../node_modules/material-design-icons/editor/svg',
            to: 'node_modules/material-design-icons/editor/svg'
        }, {
            from: '../node_modules/react-virtualized/*.css',
        }]),
        new webpack.DefinePlugin({
            'process.env.COMMIT_ID': JSON.stringify(process.env.COMMIT_ID),
        }),
    ],
};