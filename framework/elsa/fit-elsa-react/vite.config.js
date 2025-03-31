/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
import libCss from 'vite-plugin-libcss';
import {fileURLToPath, URL} from 'node:url'
import svgr from "vite-plugin-svgr";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        react(),
        libCss(),
        svgr({svgrOptions: {icon: true}})
    ],
    build: {
        lib: {
            entry: 'src/index.js',
            name: 'elsa-react',
            filename: (format) => `elsa-react.${format}.js`
        },
        sourcemap: true,
        rollupOptions: {
            external: ['react', 'react-dom', '@fit-elsa/elsa-core', 'antd', 'axios', '@monaco-editor/react'],
            output: {
                globals: {
                    react: 'react',
                    'react-dom': 'ReactDOM',
                    '@fit-elsa/elsa-core': '@fit-elsa/elsa-core',
                    '@monaco-editor/react': '@monaco-editor/react',
                },
                inlineDynamicImports: true, // 确保 worker 内联
            }
        },
        outDir: "build"
    },
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
})
