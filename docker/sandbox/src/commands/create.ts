import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import * as p from '@clack/prompts';
import pc from 'picocolors';
import {
  IMAGE_NAME, MAIN_REPO, DOCKERFILE, SCRIPTS_DIR,
  containerName, containerNameCandidates,
  worktreeDirCandidates, claudeConfigDirCandidates, codexConfigDirCandidates, opencodeConfigDirCandidates,
  sanitizeBranchName, detectHostResources, assertValidBranchName,
  parsePositiveIntegerOption,
} from '../constants.js';
import { run, runOk, runSafe } from '../shell.js';

interface CreateOptions {
  cpu?: string;
  memory?: string;
}

export async function create(branch: string, base: string | undefined, opts: CreateOptions) {
  assertValidBranchName(branch);

  const defaults = detectHostResources();
  const vmCpu = parsePositiveIntegerOption(opts.cpu, '--cpu') ?? defaults.cpu;
  const vmMemory = parsePositiveIntegerOption(opts.memory, '--memory') ?? defaults.memory;

  const safeName = sanitizeBranchName(branch);
  const container = containerName(branch);
  const worktreeCandidates = worktreeDirCandidates(branch);
  const claudeDirCandidates = claudeConfigDirCandidates(branch);
  const codexDirCandidates = codexConfigDirCandidates(branch);
  const opencodeDirCandidates = opencodeConfigDirCandidates(branch);
  const worktree = worktreeCandidates.find((dir) => fs.existsSync(dir)) ?? worktreeCandidates[0];
  const claudeDir = claudeDirCandidates.find((dir) => fs.existsSync(dir)) ?? claudeDirCandidates[0];
  const codexDir = codexDirCandidates.find((dir) => fs.existsSync(dir)) ?? codexDirCandidates[0];
  const opencodeDir = opencodeDirCandidates.find((dir) => fs.existsSync(dir)) ?? opencodeDirCandidates[0];
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
        // Remove old container if exists (also clean legacy-named containers)
        const existing = runSafe('docker', ['ps', '-a', '--format', '{{.Names}}']).split('\n');
        const matchedContainers = containerNameCandidates(branch).filter((name) => existing.includes(name));
        if (matchedContainers.length > 0) {
          message('Removing old container...');
          for (const name of matchedContainers) {
            runSafe('docker', ['stop', name]);
            runSafe('docker', ['rm', name]);
          }
        }

        const envArgs: string[] = [];
        envArgs.push('-e', 'CLAUDE_CONFIG_DIR=/home/devuser/.claude');

        // Ensure config dirs exist
        fs.mkdirSync(claudeDir, { recursive: true });
        fs.mkdirSync(codexDir, { recursive: true });
        fs.mkdirSync(opencodeDir, { recursive: true });

        // Pre-seed Codex auth from host if available
        const hostCodexAuth = path.join(process.env.HOME!, '.codex', 'auth.json');
        const sandboxCodexAuth = path.join(codexDir, 'auth.json');
        if (fs.existsSync(hostCodexAuth) && !fs.existsSync(sandboxCodexAuth)) {
          fs.copyFileSync(hostCodexAuth, sandboxCodexAuth);
        }

        // Pre-seed OpenCode auth from host if available
        const hostOpencodeAuth = path.join(process.env.HOME!, '.local', 'share', 'opencode', 'auth.json');
        const sandboxOpencodeAuth = path.join(opencodeDir, 'auth.json');
        if (fs.existsSync(hostOpencodeAuth) && !fs.existsSync(sandboxOpencodeAuth)) {
          fs.copyFileSync(hostOpencodeAuth, sandboxOpencodeAuth);
        }

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
          '-v', `${codexDir}:/home/devuser/.codex`,
          '-v', `${opencodeDir}:/home/devuser/.local/share/opencode`,
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
  const runningContainers = runSafe('docker', ['ps', '--format', '{{.Names}}']).split('\n');
  const checks = [
    { name: 'Container running', ok: runningContainers.includes(container) },
    { name: 'Java', ok: runOk('docker', ['exec', container, 'java', '-version']) },
    { name: 'Maven', ok: runOk('docker', ['exec', container, 'mvn', '--version']) },
    { name: 'Claude Code', ok: runOk('docker', ['exec', container, 'bash', '-lc', 'claude --version']) },
    { name: 'Codex', ok: runOk('docker', ['exec', container, 'bash', '-lc', 'codex --version']) },
    { name: 'OpenCode', ok: runOk('docker', ['exec', container, 'bash', '-lc', 'opencode version']) },
  ];
  for (const c of checks) {
    p.log.info(`  ${c.ok ? pc.green('✓') : pc.yellow('?')} ${c.name}`);
  }

  // Result summary
  p.log.success(pc.green('Ready!'));

  // Align management command comments dynamically
  const mgmtCmds: [string, string][] = [
    ['sandbox ls', '# 查看所有沙箱'],
    [`sandbox exec ${branch}`, '# 进入此沙箱'],
    [`sandbox rm ${branch}`, '# 清理此沙箱'],
  ];
  const maxCmdLen = Math.max(...mgmtCmds.map(([c]) => c.length));
  const mgmtLines = mgmtCmds.map(([cmd, comment]) => `  ${cmd.padEnd(maxCmdLen + 4)}${comment}`).join('\n');

  console.log(`
${pc.cyan('进入沙箱：')}
  docker exec -it ${container} bash

${pc.cyan('沙箱信息：')}
  容器名:   ${container}
  分支:     ${branch}
  Worktree: ${worktree}
  VM 资源:  ${vmCpu} CPU / ${vmMemory} GB

${pc.cyan('管理命令：')}
${mgmtLines}

${pc.cyan('Claude Code：')}
  首次使用需在容器内运行 claude 完成一次 OAuth 登录，之后免登录。
  凭据持久化：${claudeDir}/

${pc.cyan('Codex：')}
  ${fs.existsSync(path.join(codexDir, 'auth.json')) ? '已从宿主机预植入认证凭据，可直接使用。' : '首次使用需在容器内运行 codex，按 Esc 选择 Device Code 方式登录。'}
  凭据持久化：${codexDir}/

${pc.cyan('OpenCode：')}
  ${fs.existsSync(path.join(opencodeDir, 'auth.json')) ? '已从宿主机预植入认证凭据，可直接使用。' : '首次使用需在容器内配置认证凭据。'}
  凭据持久化：${opencodeDir}/
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
