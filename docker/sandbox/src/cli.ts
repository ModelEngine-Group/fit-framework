#!/usr/bin/env node
import { Command } from 'commander';
import { create } from './commands/create.js';
import { rmOne, rmAll } from './commands/rm.js';
import { ls } from './commands/ls.js';
import { enter } from './commands/enter.js';
import { vmStatus, vmStart, vmStop } from './commands/vm.js';
import { rebuild } from './commands/rebuild.js';

const program = new Command();

program
  .name('sandbox')
  .description('AI Coding Sandbox (macOS only) - 基于 Colima + Docker + Git Worktree 的隔离开发环境')
  .version('3.6.3');

// ========== create ==========
program
  .command('create')
  .description('创建沙箱（Colima VM + Docker 镜像 + Git Worktree + 容器）')
  .argument('<branch>', '分支名')
  .argument('[base]', '基础分支（默认为当前分支）')
  .option('--cpu <n>', 'VM CPU 核数')
  .option('--memory <n>', 'VM 内存 GB')
  .action(async (branch: string, base: string | undefined, opts) => {
    await create(branch, base, opts);
  });

// ========== rm ==========
program
  .command('rm')
  .description('删除沙箱（容器 + worktree + AI 工具配置）')
  .argument('[branch]', '分支名（省略时需搭配 --all）')
  .option('--all', '删除所有沙箱')
  .action(async (branch: string | undefined, opts) => {
    if (opts.all) {
      await rmAll();
    } else if (branch) {
      await rmOne(branch);
    } else {
      console.error('请指定分支名或使用 --all');
      process.exit(1);
    }
  });

// ========== ls ==========
program
  .command('ls')
  .description('列出所有沙箱状态')
  .action(() => {
    ls();
  });

// ========== exec ==========
program
  .command('exec')
  .description('进入沙箱或在沙箱内执行命令（默认打开 bash）')
  .argument('<branch>', '分支名')
  .argument('[cmd...]', '要执行的命令（默认 bash）')
  .action((branch: string, cmd: string[]) => {
    enter(branch, cmd);
  });

// ========== vm ==========
const vm = program
  .command('vm')
  .description('Colima VM 管理');

vm.command('status')
  .description('查看 VM 状态')
  .action(() => { vmStatus(); });

vm.command('start')
  .description('启动 VM')
  .option('--cpu <n>', 'CPU 核数')
  .option('--memory <n>', '内存 GB')
  .action((opts) => { vmStart(opts); });

vm.command('stop')
  .description('停止 VM')
  .action(() => { vmStop(); });

// ========== rebuild ==========
program
  .command('rebuild')
  .description('强制重建 Docker 镜像')
  .option('-q, --quiet', '静默模式，不输出构建日志')
  .action((opts) => { rebuild(opts); });

program.parse();
