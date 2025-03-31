/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const path = require("path");
module.exports = {
    mode: "production",
    entry: {
        "presentation": "./plugins/presentation/src/index.js"
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
        extensions: [".js", ".jsx"]
    },
    optimization: {
        minimize: false
    }
};
