/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {v4 as uuidv4} from 'uuid';

export const NODE_STATUS = {
  RUNNING: 'RUNNING',
  ERROR: 'ERROR',
  SUCCESS: 'ARCHIVED',
  DEFAULT: 'DEFAULT',
  UN_RUNNING: 'UN_RUNNING',
  TERMINATED: 'TERMINATED',
};

export const SECTION_TYPE = {
  CONDITION: 'condition',
  DEFAULT: 'default',
};

export const UNARY_OPERATOR = {
  IS_EMPTY: 'is empty',
  IS_NOT_EMPTY: 'is not empty',
  IS_EMPTY_STRING: 'is empty string',
  IS_NOT_EMPTY_STRING: 'is not empty string',
  IS_TRUE: 'is true',
  IS_FALSE: 'is false',
  IS_NULL: 'is null',
  IS_NOT_NULL: 'is not null',
};

export const BINARY_OPERATOR = {
  LONGER_THAN: 'longer than',
  LONGER_THAN_OR_EQUAL: 'longer than or equal',
  SHORTER_THAN: 'shorter than',
  SHORTER_THAN_OR_EQUAL: 'shorter than or equal',
  STARTS_WITH: 'starts with',
  ENDS_WITH: 'ends with',
  EQUAL: 'equal',
  NOT_EQUAL: 'not equal',
  CONTAINS: 'contains',
  DOES_NOT_CONTAIN: 'does not contain',
  GREATER_THAN: 'greater than',
  GREATER_THAN_OR_EQUAL: 'greater than or equal',
  LESS_THAN: 'less than',
  LESS_THAN_OR_EQUAL: 'less than or equal',
};

export const VIRTUAL_CONTEXT_NODE = {
  id: '_systemEnv',
  name: 'systemEnv',
};

export const VIRTUAL_CONTEXT_NODE_VARIABLES = {
  INSTANCE_ID: 'instanceId',
  APP_ID: 'appId',
  MEMORIES: 'memories',
  USE_MEMORY: 'useMemory',
  USER_ID: 'userId',
  FILE_URLS: 'fileUrls',
  CHAT_ID: 'chatId',
};

export const CONNECTOR = {
  RADIUS: 6,
  CONDITION_RADIUS: 4,
};

export const SOURCE_PLATFORM = {
  OFFICIAL: 'official',
  HUGGING_FACE: 'huggingface',
  LLAMA_INDEX: 'llamaindex',
  LANG_CHAIN: 'langchain',
};

export const DATA_TYPES = {
  STRING: 'String',
  INTEGER: 'Integer',
  BOOLEAN: 'Boolean',
  NUMBER: 'Number',
  OBJECT: 'Object',
  ARRAY: 'Array',
};

export const OBSERVER_STATUS = {
  ENABLE: 'enable',
  DISABLE: 'disable',
};

export const EVALUATION_ALGORITHM_NODE_CONST = {
  ALGORITHM: 'algorithm',
  UNIQUE_NAME: 'uniqueName',
  PASS_SCORE: 'passScore',
  IS_PASS: 'isPass',
  SCORE: 'score',
};

export const FLOW_TYPE = {
  APP: 'app',
  WORK_FLOW: 'workflow',
};

export const FROM_TYPE = {
  EXPAND: 'Expand',
  INPUT: 'Input',
  REFERENCE: 'Reference',
};

export const DEFAULT_FLOW_META = '{"triggerMode":"auto","jober":{"type":"STORE_JOBER","name":"","fitables":[],"converter":{"type":"mapping_converter"},"entity":{"uniqueName":"","params":[],"return":{"type":""}}},"joberFilter":{"type":"MINIMUM_SIZE_FILTER","threshold":1}}';

export const TOOL_TYPE = {
  WATER_FLOW: 'WATERFLOW',
  TOOL: 'TOOL',
};

export const HTTP_METHOD_TYPE = {
  GET: 'get',
  POST: 'post',
  PUT: 'put',
  DELETE: 'delete',
  PATCH: 'patch',
};

export const HTTP_BODY_TYPE = {
  X_WWW_FORM_URLENCODED: 'x-www-form-urlencoded',
  JSON: 'json',
  TEXT: 'text',
};

export const END_NODE_TYPE = {
  VARIABLES: 'variables',
  MANUAL_CHECK: 'manualCheck',
};

export const EVENT_NAME = {
  NODE_NAME_CHANGED: 'NODE_NAME_CHANGED',
};

export const DEFAULT_KNOWLEDGE_REPO_GROUP = 'default';

export const DEFAULT_AP_PROMPT_MODEL_CONFIG = {
  MODEL: 'Qwen1.5-32B-Chat',
  TEMPERATURE: '0.3',
};
export const SYSTEM_ACTION = {
  JADE_NODE_CONFIG_CHANGE: 'jade_node_config_change',
  PAGE_DATA_CHANGED: 'page_data_changed',
};

export const DEFAULT_MAX_MEMORY_ROUNDS = {
  id: uuidv4(),
  name: 'maxMemoryRounds',
  type: DATA_TYPES.INTEGER,
  from: FROM_TYPE.INPUT,
  value: '3',
};

export const DEFAULT_LLM_KNOWLEDGE_BASES = {
  id: uuidv4(),
  from: FROM_TYPE.EXPAND,
  name: 'knowledgeBases',
  type: DATA_TYPES.ARRAY,
  value: [],
};

export const DEFAULT_LLM_REFERENCE_OUTPUT = {
  id: uuidv4(),
  from: FROM_TYPE.INPUT,
  name: 'reference',
  description: '',
  type: DATA_TYPES.ARRAY,
  value: [],
};

export const DEFAULT_KNOWLEDGE_REPO_GROUP_STRUCT = {
  id: uuidv4(),
  name: 'groupId',
  type: DATA_TYPES.STRING,
  from: FROM_TYPE.INPUT,
  value: DEFAULT_KNOWLEDGE_REPO_GROUP,
};

export const RENDER_TYPE = {
  SELECT: 'Select',
  RADIO: 'Radio',
  INPUT: 'Input',
  SWITCH: 'Switch',
  CHECK_BOX: 'CheckBox',
  LABEL: 'Label',
};

export const DEFAULT_ADD_TOOL_NODE_CONTEXT = {
  id: uuidv4(),
  name: 'context',
  type: DATA_TYPES.OBJECT,
  from: FROM_TYPE.EXPAND,
  value: [{
    id: uuidv4(),
    name: VIRTUAL_CONTEXT_NODE_VARIABLES.INSTANCE_ID,
    type: DATA_TYPES.STRING,
    from: FROM_TYPE.REFERENCE,
    referenceId: VIRTUAL_CONTEXT_NODE_VARIABLES.INSTANCE_ID,
    referenceKey: VIRTUAL_CONTEXT_NODE_VARIABLES.INSTANCE_ID,
    referenceNode: VIRTUAL_CONTEXT_NODE.id,
    value: [VIRTUAL_CONTEXT_NODE_VARIABLES.INSTANCE_ID],
  }],
};

export const DEFAULT_KNOWLEDGE_RETRIEVAL_NODE_USER_ID = {
  id: `userId_${uuidv4()}`,
  name: 'userId',
  type: 'String',
  from: 'Reference',
  referenceNode: VIRTUAL_CONTEXT_NODE.id,
  referenceId: VIRTUAL_CONTEXT_NODE_VARIABLES.USER_ID,
  referenceKey: VIRTUAL_CONTEXT_NODE_VARIABLES.USER_ID,
  editable: false,
  value: [VIRTUAL_CONTEXT_NODE_VARIABLES.USER_ID],
};