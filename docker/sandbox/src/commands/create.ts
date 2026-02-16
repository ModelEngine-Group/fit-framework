import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import * as p from '@clack/prompts';
import pc from 'picocolors';
import {
  IMAGE_NAME, MAIN_REPO, DOCKERFILE, SCRIPTS_DIR,
  containerName, containerNameCandidates,
  worktreeDirCandidates,
  sanitizeBranchName, detectHostResources, assertValidBranchName,
  parsePositiveIntegerOption,
} from '../constants.js';
import { AI_TOOLS, toolConfigDirCandidates, toolNpmPackagesArg } from '../tools.js';
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
  const worktree = worktreeCandidates.find((dir) => fs.existsSync(dir)) ?? worktreeCandidates[0];
  const baseBranch = base ?? runSafe('git', ['-C', MAIN_REPO, 'branch', '--show-current']);

  // Resolve per-branch config directory for each AI tool
  const resolvedTools = AI_TOOLS.map((tool) => {
    const candidates = toolConfigDirCandidates(tool, branch);
    return { tool, dir: candidates.find((d) => fs.existsSync(d)) ?? candidates[0] };
  });

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
          '--build-arg', `AI_TOOL_PACKAGES=${toolNpmPackagesArg()}`,
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

        // Ensure config dirs exist + pre-seed auth & config from host
        for (const { tool, dir } of resolvedTools) {
          fs.mkdirSync(dir, { recursive: true });
          if (tool.hostAuthFile && tool.authFileName) {
            const sandboxAuth = path.join(dir, tool.authFileName);
            if (fs.existsSync(tool.hostAuthFile) && !fs.existsSync(sandboxAuth)) {
              fs.mkdirSync(path.dirname(sandboxAuth), { recursive: true });
              fs.copyFileSync(tool.hostAuthFile, sandboxAuth);
            }
          }
          for (const { hostPath, sandboxName } of tool.hostPreSeedFiles ?? []) {
            const dest = path.join(dir, sandboxName);
            if (fs.existsSync(hostPath) && !fs.existsSync(dest)) {
              fs.mkdirSync(path.dirname(dest), { recursive: true });
              fs.copyFileSync(hostPath, dest);
            }
          }
        }

        // Build env args and volume mounts from tool registry
        const envArgs = resolvedTools.flatMap(({ tool }) =>
          Object.entries(tool.envVars ?? {}).flatMap(([k, v]) => ['-e', `${k}=${v}`])
        );
        const toolVolumes = resolvedTools.flatMap(({ tool, dir }) =>
          ['-v', `${dir}:${tool.containerMount}`]
        );

        run('docker', [
          'run', '-d',
          '--name', container,
          '--hostname', 'fit-sandbox',
          '--label', 'fit-sandbox',
          '--label', `fit-sandbox.branch=${branch}`,
          '-v', `${worktree}:/workspace`,
          '-v', `${MAIN_REPO}/.git:${MAIN_REPO}/.git`,
          '-v', `${process.env.HOME}/.ssh:/home/devuser/.ssh:ro`,
          ...toolVolumes,
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
  ];
  const toolChecks = AI_TOOLS.map((tool) => ({
    tool,
    ok: runOk('docker', ['exec', container, 'bash', '-lc', tool.versionCmd]),
  }));
  for (const c of checks) {
    p.log.info(`  ${c.ok ? pc.green('✓') : pc.yellow('?')} ${c.name}`);
  }
  for (const c of toolChecks) {
    p.log.info(`  ${c.ok ? pc.green('✓') : pc.yellow('?')} ${c.tool.name}`);
    if (!c.ok) {
      p.log.warn(`    ${c.tool.name} 未安装或不可用（期望 npm 包：${c.tool.npmPackage}），可运行 sandbox rebuild`);
    }
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

  // Tool credential hints
  const toolHints = resolvedTools.map(({ tool, dir }) => {
    const hasAuth = tool.authFileName && fs.existsSync(path.join(dir, tool.authFileName));
    const hint = hasAuth ? '已从宿主机预植入认证凭据，可直接使用。' : tool.noAuthHint;
    return `${pc.cyan(`${tool.name}：`)}\n  ${hint}\n  凭据持久化：${dir}/`;
  }).join('\n\n');

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

${toolHints}
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
