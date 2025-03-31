/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import en from './en_US.json';
import zh from './zh_CN.json';

const resources = {
  en: {
    translation: en,
  },
  zh: {
    translation: zh,
  },
};

i18n.use(initReactI18next).init({
  resources,
  fallbackLng: 'zh-cn',
  interpolation: {
    escapeValue: false,
  },
  returnNull: false,
});

export default i18n;
