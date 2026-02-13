import fs from 'node:fs';
import * as p from '@clack/prompts';
import pc from 'picocolors';
import { WORKTREE_BASE, CLAUDE_SANDBOX_BASE } from '../constants.js';
import { runSafe } from '../shell.js';

export function ls() {
  p.intro(pc.cyan('沙箱状态'));

  // Containers
  const containers = runSafe('docker', [
    'ps', '-a', '--filter', 'label=fit-sandbox',
    '--format', 'table {{.Names}}\t{{.Status}}\t{{.Label "fit-sandbox.branch"}}',
  ]);

  p.log.step('容器：');
  if (!containers || containers.split('\n').length <= 1) {
    p.log.warn('  没有沙箱容器');
  } else {
    for (const line of containers.split('\n')) {
      console.log(`  ${line}`);
    }
  }

  // Worktrees
  p.log.step('Worktrees：');
  if (fs.existsSync(WORKTREE_BASE)) {
    const entries = fs.readdirSync(WORKTREE_BASE);
    if (entries.length > 0) {
      for (const entry of entries) {
        console.log(`  ${entry} -> ${WORKTREE_BASE}/${entry}`);
      }
    } else {
      p.log.warn('  没有 worktree 目录');
    }
  } else {
    p.log.warn('  没有 worktree 目录');
  }

  // Claude config dirs
  p.log.step('Claude 配置：');
  if (fs.existsSync(CLAUDE_SANDBOX_BASE)) {
    const entries = fs.readdirSync(CLAUDE_SANDBOX_BASE);
    if (entries.length > 0) {
      for (const entry of entries) {
        console.log(`  ${entry} -> ${CLAUDE_SANDBOX_BASE}/${entry}`);
      }
    } else {
      p.log.warn('  没有 Claude 沙箱配置');
    }
  } else {
    p.log.warn('  没有 Claude 沙箱配置');
  }

}
