import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import * as p from '@clack/prompts';
import pc from 'picocolors';
import {
  IMAGE_NAME, MAIN_REPO, DOCKERFILE, SCRIPTS_DIR,
  containerName, worktreeDir, claudeConfigDir, sanitizeBranchName,
  detectHostResources,
} from '../constants.js';
import { run, runOk, runSafe } from '../shell.js';

interface CreateOptions {
  cpu?: string;
  memory?: string;
}

export async function create(branch: string, base: string | undefined, opts: CreateOptions) {
  const defaults = detectHostResources();
  const vmCpu = opts.cpu ? Number(opts.cpu) : defaults.cpu;
  const vmMemory = opts.memory ? Number(opts.memory) : defaults.memory;

  const safeName = sanitizeBranchName(branch);
  const container = containerName(branch);
  const worktree = worktreeDir(branch);
  const claudeDir = claudeConfigDir(branch);
  const baseBranch = base ?? runSafe('git', ['-C', MAIN_REPO, 'branch', '--show-current']);

  p.intro(pc.cyan('AI Coding Sandbox (Colima)'));
  p.log.info(`Branch: ${pc.bold(branch)} | Base: ${pc.bold(baseBranch)} | VM: ${vmCpu} CPU / ${vmMemory} GB`);

  await p.tasks([
    {
      title: 'Checking Colima installation',
      task: async (message) => {
        if (!runOk('command', ['-v', 'colima'])) {
          // command -v doesn't work via execFile; check with which
          if (!runOk('which', ['colima'])) {
            message('Installing colima + docker...');
            run('brew', ['install', 'colima', 'docker']);
          }
        }
        return 'Colima OK';
      },
    },
    {
      title: 'Starting Colima VM',
      task: async () => {
        if (runOk('colima', ['status'])) {
          return 'Already running';
        }
        const arch = runSafe('uname', ['-m']);
        const args = ['start', '--cpu', String(vmCpu), '--memory', String(vmMemory), '--disk', '60'];
        if (arch === 'arm64') {
          args.push('--arch', 'aarch64', '--vm-type=vz', '--mount-type=virtiofs');
        } else {
          args.push('--arch', 'x86_64');
        }
        run('colima', args);
        return 'Colima started';
      },
    },
    {
      title: 'Building sandbox image',
      task: async (message) => {
        if (runOk('docker', ['image', 'inspect', IMAGE_NAME])) {
          return `Image exists (docker rmi ${IMAGE_NAME} to rebuild)`;
        }
        message('Building (first time takes a few minutes)...');
        const hostUid = run('id', ['-u']);
        const hostGid = run('id', ['-g']);
        run('docker', [
          'build', '-t', IMAGE_NAME,
          '--build-arg', `HOST_UID=${hostUid}`,
          '--build-arg', `HOST_GID=${hostGid}`,
          '-f', DOCKERFILE, '.',
        ], { cwd: SCRIPTS_DIR });
        return 'Image built';
      },
    },
    {
      title: 'Setting up git worktree',
      task: async (message) => {
        fs.mkdirSync(worktree, { recursive: true });
        // Check if worktree dir already has content (re-use existing)
        if (fs.readdirSync(worktree).length > 0) {
          return `Worktree exists at ${worktree}`;
        }
        // Remove the empty dir so git worktree add can create it
        fs.rmdirSync(worktree);

        const branchExists = runOk('git', ['-C', MAIN_REPO, 'show-ref', '--verify', '--quiet', `refs/heads/${branch}`]);
        if (branchExists) {
          message(`Branch '${branch}' exists, creating worktree...`);
          run('git', ['-C', MAIN_REPO, 'worktree', 'add', worktree, branch]);
        } else {
          message(`Creating new branch '${branch}' from '${baseBranch}'...`);
          run('git', ['-C', MAIN_REPO, 'worktree', 'add', '-b', branch, worktree, baseBranch]);
        }
        return `Worktree at ${worktree}`;
      },
    },
    {
      title: `Starting container '${container}'`,
      task: async (message) => {
        // Remove old container if exists
        const existing = runSafe('docker', ['ps', '-a', '--format', '{{.Names}}']);
        if (existing.split('\n').includes(container)) {
          message('Removing old container...');
          runSafe('docker', ['stop', container]);
          runSafe('docker', ['rm', container]);
        }

        const envArgs: string[] = [];
        envArgs.push('-e', 'CLAUDE_CONFIG_DIR=/home/devuser/.claude');

        // Ensure claude config dir exists
        fs.mkdirSync(claudeDir, { recursive: true });

        run('docker', [
          'run', '-d',
          '--name', container,
          '--hostname', 'fit-sandbox',
          '--label', 'fit-sandbox',
          '--label', `fit-sandbox.branch=${branch}`,
          '-v', `${worktree}:/workspace`,
          '-v', `${MAIN_REPO}/.git:${MAIN_REPO}/.git`,
          '-v', `${process.env.HOME}/.ssh:/home/devuser/.ssh:ro`,
          '-v', `${claudeDir}:/home/devuser/.claude`,
          ...envArgs,
          '-w', '/workspace',
          IMAGE_NAME,
        ]);

        // Sync git config
        message('Syncing git config...');
        syncGitConfig(container);

        return 'Container started';
      },
    },
  ]);

  // Verify
  p.log.step('Verifying setup...');
  const checks = [
    { name: 'Container running', ok: runOk('docker', ['ps', '--format', '{{.Names}}']) },
    { name: 'Java', ok: runOk('docker', ['exec', container, 'java', '-version']) },
    { name: 'Maven', ok: runOk('docker', ['exec', container, 'mvn', '--version']) },
    { name: 'Claude Code', ok: runOk('docker', ['exec', container, 'bash', '-lc', 'claude --version']) },
    { name: 'Codex', ok: runOk('docker', ['exec', container, 'bash', '-lc', 'codex --version']) },
  ];
  for (const c of checks) {
    p.log.info(`  ${c.ok ? pc.green('✓') : pc.yellow('?')} ${c.name}`);
  }

  // Result summary
  p.log.success(pc.green('Ready!'));
  console.log(`
${pc.cyan('进入沙箱：')}
  docker exec -it ${container} bash

${pc.cyan('沙箱信息：')}
  容器名:   ${container}
  分支:     ${branch}
  Worktree: ${worktree}
  VM 资源:  ${vmCpu} CPU / ${vmMemory} GB

${pc.cyan('管理命令：')}
  sandbox.sh ls                    # 查看所有沙箱
  sandbox.sh exec ${safeName}      # 进入此沙箱
  sandbox.sh rm ${safeName}        # 清理此沙箱

${pc.cyan('Claude Code：')}
  首次使用需在容器内运行 claude 完成一次 OAuth 登录，之后免登录。
  凭据持久化：~/.claude-sandboxes/${safeName}/
`);
}

function syncGitConfig(container: string) {
  const home = process.env.HOME!;
  const containerHome = '/home/devuser';
  const gitconfigPath = path.join(home, '.gitconfig');

  if (!fs.existsSync(gitconfigPath)) return;

  // Read .gitconfig, fix paths, strip macOS-only tool sections
  let gitconfig = fs.readFileSync(gitconfigPath, 'utf8');
  gitconfig = gitconfig.replaceAll(home, containerHome);
  // Remove sourcetree sections
  gitconfig = gitconfig.replace(/\[difftool "sourcetree"\][^\[]*/gs, '');
  gitconfig = gitconfig.replace(/\[mergetool "sourcetree"\][^\[]*/gs, '');

  // Pipe into container
  execFileSync('docker', ['exec', '-i', container, 'sh', '-c', `cat > ${containerHome}/.gitconfig`], {
    input: gitconfig,
    stdio: ['pipe', 'pipe', 'pipe'],
  });

  // Add safe.directory entries
  runSafe('docker', ['exec', container, 'git', 'config', '--global', '--add', 'safe.directory', '/workspace']);
  runSafe('docker', ['exec', container, 'git', 'config', '--global', '--add', 'safe.directory', MAIN_REPO]);

  // Copy referenced files
  for (const f of ['.gitignore_global', '.stCommitMsg']) {
    const hostFile = path.join(home, f);
    if (fs.existsSync(hostFile)) {
      runSafe('docker', ['cp', hostFile, `${container}:${containerHome}/${f}`]);
    }
  }
}
