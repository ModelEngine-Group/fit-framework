import fs from 'node:fs';
import * as p from '@clack/prompts';
import pc from 'picocolors';
import { WORKTREE_BASE } from '../constants.js';
import { AI_TOOLS } from '../tools.js';
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

  // AI tool config dirs
  for (const tool of AI_TOOLS) {
    p.log.step(`${tool.name} 配置：`);
    if (fs.existsSync(tool.sandboxBase)) {
      const entries = fs.readdirSync(tool.sandboxBase);
      if (entries.length > 0) {
        for (const entry of entries) {
          console.log(`  ${entry} -> ${tool.sandboxBase}/${entry}`);
        }
      } else {
        p.log.warn(`  没有 ${tool.name} 沙箱配置`);
      }
    } else {
      p.log.warn(`  没有 ${tool.name} 沙箱配置`);
    }
  }

}
