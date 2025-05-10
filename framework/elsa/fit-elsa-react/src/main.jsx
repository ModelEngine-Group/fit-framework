/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import i18n from './i18n/i18n.js';

ReactDOM.createRoot(document.getElementById('root')).render(
  // 打开strictMode会导致每个组件被加载两次，测试某些功能时可以打开
  // <React.StrictMode>
  <App i18n={i18n}/>,
  // </React.StrictMode>,
);
