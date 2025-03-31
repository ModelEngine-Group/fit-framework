/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker&inline';
import 'monaco-editor/esm/vs/basic-languages/python/python.contribution'; // 代码高亮&提示
import 'monaco-editor/esm/vs/editor/contrib/folding/browser/folding.js'; // 折叠
import 'monaco-editor/esm/vs/editor/contrib/suggest/browser/suggestController.js'; // 代码补全和建议
import {useEffect, useRef} from 'react';
import PropTypes from 'prop-types';

self.MonacoEnvironment = {
  getWorker() {
    return new editorWorker();
  },
};

/**
 * 代码编辑器.
 *
 * @param language 默认语言.
 * @param code 默认代码.
 * @param onChange 数据变化时触发.
 * @param theme 主题.
 * @param isReadOnly 是否只读.
 * @param suggestions 自动补全. 格式: {label: "args.input", insertText: "args.input"} .
 * @return {JSX.Element}
 * @constructor
 */
export const CodeEditor = (
  {
    language,
    code,
    onChange,
    theme = 'vs-dark',
    options = {readOnly: true},
    suggestions = [],
  }) => {
  const monacoRef = useRef(null);
  const providerRef = useRef(null);
  const divRef = useRef();

  useEffect(() => {
    if (suggestions.length <= 0) {
      return;
    }
    // 在每次 suggestions 变化时重新注册补全提示
    !options.readOnly && registerSuggestions();
  }, [suggestions]);

  const registerSuggestions = () => {
    // 如果存在，注销之后重新注册
    if (providerRef.current) {
      providerRef.current.dispose();
    }
    providerRef.current = monaco.languages.registerCompletionItemProvider(language, {
      provideCompletionItems: () => {
        return {
          suggestions: suggestions.map(s => {
            return {
              label: s.label,
              kind: monaco.languages.CompletionItemKind.Field,
              insertText: _getInsertText(language, s),
              insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            };
          }),
        };
      },
    });
  };

  useEffect(() => {
    monacoRef.current = monaco.editor.create(divRef.current, {
      value: code,
      language: language,
      lineNumbers: 'on',
      roundedSelection: false,
      scrollBeyondLastLine: false,
      readOnly: options.readOnly,
      theme: theme,
      automaticLayout: true,
      contextmenu: false,
      fontSize: 14,
      fontFamily: 'inherit',
    });
    monacoRef.current.onDidChangeModelContent(() => {
      onChange && onChange(monacoRef.current.getModel().getValue());
    });
    registerSuggestions();
    return () => {
      monacoRef.current.dispose();
    };
  }, []);

  // code改变时，设置model中的值.
  useEffect(() => {
    monacoRef.current && monacoRef.current.getModel().setValue(code);
  }, [code]);

  /**
   * 构造对应语言的代码补全提示
   *
   * @param language 语言
   * @param suggestion 注册的提示对象
   * @return {*|string} 代码补全提示
   * @private
   */
  const _getInsertText = (language, suggestion) => {
    if (language === 'json') {
      return `"${suggestion.insertText}": ""`;
    }
    return suggestion.insertText;
  };

  return (<>
    <div ref={divRef} className={'jade-code-editor'} style={{width: '100%', height: '100%'}}/>
  </>);
};

CodeEditor.propTypes = {
  language: PropTypes.string.isRequired,
  height: PropTypes.number,
  code: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  theme: PropTypes.string,
  options: PropTypes.object,
  suggestions: PropTypes.array,
};