import assert from 'node:assert/strict';
import test from 'node:test';
import { AI_TOOLS } from './tools.js';

test('所有工具都必须包含非空 setupHint', () => {
  for (const tool of AI_TOOLS) {
    assert.equal(typeof tool.setupHint, 'string');
    assert.ok(tool.setupHint.trim().length > 0, `${tool.name} 的 setupHint 不能为空`);
  }
});
