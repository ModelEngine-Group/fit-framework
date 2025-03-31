/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const path = require("path");

const dailyConfig = require("./config/daily-config.json");
const productConfig = require("./config/product-config.json");
const webpack = require("webpack");

function composeConfig(env) {
    return env === "daily" ? {...dailyConfig} : {...productConfig};
}

const minimize = process.env.MODE !== "debug";

module.exports = {
    mode: "production",
    entry: {
        "elsa": "./core/src/index.js"
    },
    output: {
        filename: "[name].js",
        path: path.resolve(__dirname, "build"),
        library: {
            type: "module"
        }
    },
    experiments: {
        outputModule: true,
    },
    module: {
        rules: [
            {
                test: /\.(js)$/,
                exclude: [/node_modules/],
                loader: "babel-loader",
            },
        ],
    },
    resolve: {
        extensions: [".js"]
    },
    optimization: {
        minimize: minimize
    },
    plugins: [
        new webpack.DefinePlugin({
            __APP_CONFIG__: JSON.stringify(composeConfig(process.env.NODE_ENV))
        })
    ]
};
