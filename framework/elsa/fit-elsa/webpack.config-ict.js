/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const path = require("path");
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    entry: {
        "view": "./demos/ict-demo/ict.js",
    },
    output: {
        filename: "[name].js",
        path: path.resolve(__dirname, "build"),
    },
    module: {
        rules: [
            {
                test: /\.(js)$/,
                exclude: [/node_modules/],
                loader: "babel-loader",
            },
            {
                test: /\.css$/, // 正则表达式，表示打包.css后缀的文件
                use: ['style-loader', 'css-loader'] // 针对css文件使用的loader，注意有先后顺序，数组项越靠后越先执行
            },
            {
                // 图片打包
                test: /\.(png|jpg|gif|svg)$/,
                loader: 'url-loader',
                options: {
                    name: './images/[name].[ext]',
                    limit: 8192
                }
            }
        ],
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: './demos/ict-demo/ict.html'
        })
    ],
    optimization: {
        minimize: true
    },
};
