/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import en from './en_US.json';
import zh from './zh_CN.json';
import coreEn from '@fit-elsa/elsa-core/locales/en.json';
import coreZh from '@fit-elsa/elsa-core/locales/zh.json';

const mergeTranslations = (local, core) => {
  return { ...core, ...local }; // core 的翻译作为基础，本地翻译优先级更高
};

const resources = {
  en: {
    translation: mergeTranslations(en, coreEn),
  },
  zh: {
    translation: mergeTranslations(zh, coreZh),
  },
};

i18n.use(initReactI18next).init({
  resources,
  fallbackLng: 'zh',
  interpolation: {
    escapeValue: false,
  },
  returnNull: false,
});

export default i18n;
